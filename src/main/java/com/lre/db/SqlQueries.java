package com.lre.db;

public class SqlQueries {

    public static final String TXN_SUMMARY_SQL = """
            WITH PassTransactions AS (
                SELECT
                    vg."Group Name" AS Script_Name,
                    EMAP."Event Name" AS Transaction_Name,
                    EM."Value" - COALESCE(EM."Think Time", 0) AS Response_Time,
                    EM.Acount AS Count,
                    SUM(EM.Acount) OVER (
                        PARTITION BY vg."Group Name", EMAP."Event Name"
                        ORDER BY EM."Value" - COALESCE(EM."Think Time", 0)
                        ROWS UNBOUNDED PRECEDING
                    ) AS Running_Count,
                    SUM(EM.Acount) OVER (
                        PARTITION BY vg."Group Name", EMAP."Event Name"
                    ) AS Total_Count
                FROM Event_meter EM
                JOIN Event_map EMAP ON EM."Event ID" = EMAP."Event ID" AND EMAP."Event Type" = 'Transaction'
                JOIN TransactionEndStatus TES ON EM.Status1 = TES.Status1
                JOIN VuserGroup vg ON EM."Group ID" = vg."Group ID"
                WHERE TES."Transaction End Status" = 'Pass'
                  AND EM."Value" - COALESCE(EM."Think Time", 0) > 0
                  AND EM.Acount > 0
            ),
            PercentileValues AS (
                SELECT
                    Script_Name,
                    Transaction_Name,
                    MIN(CASE WHEN Running_Count >= Total_Count * 0.50 THEN Response_Time END) AS P50,
                    MIN(CASE WHEN Running_Count >= Total_Count * 0.90 THEN Response_Time END) AS P90,
                    MIN(CASE WHEN Running_Count >= Total_Count * 0.95 THEN Response_Time END) AS P95,
                    MIN(CASE WHEN Running_Count >= Total_Count * 0.99 THEN Response_Time END) AS P99
                FROM PassTransactions
                GROUP BY Script_Name, Transaction_Name
            ),
            SummaryMetrics AS (
                SELECT
                    vg."Group Name" AS Script_Name,
                    EMAP."Event Name" AS Transaction_Name,
                    CAST(SUM(EM.Acount) AS INTEGER) AS Transaction_Count,
                    ROUND(MIN(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM."Value" - COALESCE(EM."Think Time", 0) END), 3) AS Minimum,
                    ROUND(MAX(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM."Value" - COALESCE(EM."Think Time", 0) END), 3) AS Maximum,
                    ROUND(
                        SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN (EM."Value" - COALESCE(EM."Think Time", 0)) * EM.Acount ELSE 0 END) /
                        NULLIF(SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM.Acount ELSE 0 END), 0),
                    3) AS Average,
                    ROUND(
                        CASE WHEN SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM.Acount ELSE 0 END) > 0 THEN
                            SQRT(
                                (SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM.Acount * POWER(EM."Value" - COALESCE(EM."Think Time", 0), 2) ELSE 0 END) /
                                SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM.Acount ELSE 0 END)) -
                                POWER(
                                    SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN (EM."Value" - COALESCE(EM."Think Time", 0)) * EM.Acount ELSE 0 END) /
                                    SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM.Acount ELSE 0 END),
                                2)
                            )
                        ELSE 0 END,
                    3) AS Std_Deviation,
                    CAST(SUM(CASE WHEN TES."Transaction End Status" = 'Pass' THEN EM.Acount ELSE 0 END) AS INTEGER) AS Pass,
                    CAST(SUM(CASE WHEN TES."Transaction End Status" = 'Fail' THEN EM.Acount ELSE 0 END) AS INTEGER) AS Fail
                FROM Event_meter EM
                JOIN Event_map EMAP ON EM."Event ID" = EMAP."Event ID" AND EMAP."Event Type" = 'Transaction'
                JOIN TransactionEndStatus TES ON EM.Status1 = TES.Status1
                JOIN VuserGroup vg ON EM."Group ID" = vg."Group ID"
                GROUP BY vg."Group Name", EMAP."Event Name"
            )
            SELECT
                sm.Script_Name,
                sm.Transaction_Name,
                sm.Transaction_Count,
                sm.Minimum,
                sm.Maximum,
                sm.Average,
                sm.Std_Deviation,
                sm.Pass,
                sm.Fail,
                ROUND(COALESCE(pv.P50, 0), 3) AS p50,
                ROUND(COALESCE(pv.P90, 0), 3) AS p90,
                ROUND(COALESCE(pv.P95, 0), 3) AS p95,
                ROUND(COALESCE(pv.P99, 0), 3) AS p99
            FROM SummaryMetrics sm
            LEFT JOIN PercentileValues pv ON sm.Script_Name = pv.Script_Name AND sm.Transaction_Name = pv.Transaction_Name
            ORDER BY sm.Script_Name, sm.Transaction_Name;
            """;


    public static final String TXN_DETAILS_SQL = """
            WITH GenreSpend AS (
                SELECT c.CustomerId, c.FirstName, c.LastName, g.Name AS GenreName,
                       SUM(ii.Quantity * ii.UnitPrice) AS TotalSpent,
                       COUNT(ii.TrackId) AS TrackPurchases,
                       MAX(i.InvoiceDate) AS LastPurchaseDate,
                       COUNT(DISTINCT i.InvoiceId) AS InvoiceCount
                FROM customers c
                JOIN invoices i ON c.CustomerId = i.CustomerId
                JOIN invoice_items ii ON i.InvoiceId = ii.InvoiceId
                JOIN tracks t ON ii.TrackId = t.TrackId
                JOIN genres g ON t.GenreId = g.GenreId
                GROUP BY c.CustomerId, g.Name
            ),
            RankedSpend AS (
                SELECT CustomerId, FirstName, LastName, GenreName, TotalSpent, TrackPurchases, LastPurchaseDate, InvoiceCount,
                       RANK() OVER (PARTITION BY GenreName ORDER BY TotalSpent DESC, TrackPurchases DESC) AS GenreRank
                FROM GenreSpend
            )
            SELECT GenreName as Script_Name, CustomerId, FirstName, LastName,  TotalSpent, TrackPurchases, LastPurchaseDate, InvoiceCount
            FROM RankedSpend
            WHERE GenreRank <= 3
            ORDER BY GenreName, GenreRank;""";


    public static final String ERROR_SUMMARY_SQL = """
            WITH GenreTrackPurchase AS (
                SELECT g.Name AS GenreName, t.TrackId, t.Name AS TrackName,
                       SUM(ii.Quantity) AS TotalQuantityPurchased
                FROM tracks t
                JOIN genres g ON t.GenreId = g.GenreId
                JOIN invoice_items ii ON t.TrackId = ii.TrackId
                GROUP BY g.Name, t.TrackId
            ),
            RankedTracks AS (
                SELECT GenreName, TrackId, TrackName, TotalQuantityPurchased,
                       RANK() OVER (PARTITION BY GenreName ORDER BY TotalQuantityPurchased DESC) AS TrackRank
                FROM GenreTrackPurchase
            )
            SELECT GenreName as Script_Name, TrackId, TrackName, TotalQuantityPurchased
            FROM RankedTracks
            WHERE TrackRank <= 3
            ORDER BY GenreName, TrackRank;
            """;


    public static final String TXN_SUMMARY_STEADY_STATE_SQL = """
            WITH GenreSpend AS (
                SELECT g.Name AS GenreName, c.CustomerId, c.FirstName, c.LastName,
                       SUM(ii.Quantity * ii.UnitPrice) AS TotalSpent,
                       COUNT(ii.TrackId) AS TrackPurchases,
                       MAX(i.InvoiceDate) AS LastPurchaseDate
                FROM customers c
                JOIN invoices i ON c.CustomerId = i.CustomerId
                JOIN invoice_items ii ON i.InvoiceId = ii.InvoiceId
                JOIN tracks t ON ii.TrackId = t.TrackId
                JOIN genres g ON t.GenreId = g.GenreId
                WHERE g.Name = ?
                AND i.InvoiceDate BETWEEN ? AND ?
                GROUP BY c.CustomerId, g.Name
            )
            SELECT GenreName as Script_Name, CustomerId, FirstName, LastName, TotalSpent, TrackPurchases, LastPurchaseDate
            FROM GenreSpend
            ORDER BY TotalSpent DESC;
            
            """;


}

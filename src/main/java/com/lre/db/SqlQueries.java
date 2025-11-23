package com.lre.db;

public class SqlQueries {

    public static final String TXN_SUMMARY_SQL = """
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


    public static final String TRANSACTIONS_BY_CUSTOMER_AND_DATE_SQL = """
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

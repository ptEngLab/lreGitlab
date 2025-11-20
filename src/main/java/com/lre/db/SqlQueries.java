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
                SELECT CustomerId, FirstName, LastName, GenreName, TotalSpent, TrackPurchases, LastPurchaseDate, InvoiceCount
                FROM RankedSpend
                WHERE GenreRank <= 3
                ORDER BY GenreName, GenreRank;""";
}

package com.lre.common.utils;

public final class TableFormatter {

    private TableFormatter() {
        // utility class
    }

    public static String format(String[][] data) {
        return format(null, data);
    }

    public static String format(String[] header, String[][] dataRows) {
        if (isEmpty(header) && isEmpty(dataRows)) {
            return "";
        }

        int columns = determineColumnCount(header, dataRows);
        int[] columnWidths = computeColumnWidths(columns, header, dataRows);

        String border = buildBorder(columnWidths);
        String rowFormat = buildRowFormat(columnWidths);

        StringBuilder sb = new StringBuilder("\n");
        sb.append(border);

        if (!isEmpty(header)) {
            sb.append(String.format(rowFormat, (Object[]) header));
            sb.append(border);
        }

        if (!isEmpty(dataRows)) {
            for (String[] row : dataRows) {
                sb.append(String.format(rowFormat, (Object[]) row));
            }
        }

        sb.append(border);
        return sb.toString();
    }

    private static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    private static boolean isEmpty(String[][] arr) {
        return arr == null || arr.length == 0;
    }

    private static int determineColumnCount(String[] header, String[][] dataRows) {
        if (!isEmpty(header)) return header.length;
        return dataRows[0].length;
    }

    private static int[] computeColumnWidths(int columns, String[] header, String[][] dataRows) {
        int[] widths = new int[columns];

        for (int i = 0; i < columns; i++) {
            if (!isEmpty(header) && header[i] != null) {
                widths[i] = header[i].length();
            }

            if (!isEmpty(dataRows)) {
                for (String[] row : dataRows) {
                    if (i < row.length && row[i] != null) {
                        widths[i] = Math.max(widths[i], row[i].length());
                    }
                }
            }
        }

        return widths;
    }

    private static String buildBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.append("\n").toString();
    }

    private static String buildRowFormat(int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int w : widths) {
            sb.append(" %-").append(w).append("s |");
        }
        return sb.append("\n").toString();
    }
}

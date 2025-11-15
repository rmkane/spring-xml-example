package org.example.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for formatting text in bordered boxes with header, body, and footer sections.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoxFormatter {


    /**
     * Prints text lines inside a bordered box with a header, body, and optional footer section.
     *
     * @param header the header text
     * @param bodyLines the lines of text to print in the body
     * @param footerLines the lines of text to print in the footer (optional)
     */
    public static void printBoxed(String header, List<String> bodyLines, List<String> footerLines) {
        // Find the longest line (header, body, or footer) to determine box width
        int headerWidth = header != null ? header.length() : 0;
        int maxBodyWidth = bodyLines != null ? bodyLines.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0) : 0;
        int maxFooterWidth = footerLines != null ? footerLines.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0) : 0;
        int maxWidth = Math.max(Math.max(headerWidth, maxBodyWidth), maxFooterWidth);
        
        // Add padding (2 spaces on each side)
        int boxWidth = maxWidth + 4;
        String horizontalBorder = "┌" + "─".repeat(boxWidth - 2) + "┐";
        String horizontalSeparator = "├" + "─".repeat(boxWidth - 2) + "┤";
        String horizontalFooter = "└" + "─".repeat(boxWidth - 2) + "┘";
        
        // Print top border
        System.out.println(horizontalBorder);
        
        // Print header section
        if (header != null && !header.isEmpty()) {
            int headerPadding = maxWidth - header.length();
            String paddedHeader = "│ " + header + " ".repeat(headerPadding) + " │";
            System.out.println(paddedHeader);
            
            // Print separator between header and body
            if (bodyLines != null && !bodyLines.isEmpty()) {
                System.out.println(horizontalSeparator);
            }
        }
        
        // Print body lines (no separators between body lines)
        if (bodyLines != null) {
            for (String line : bodyLines) {
                int padding = maxWidth - line.length();
                String paddedLine = "│ " + line + " ".repeat(padding) + " │";
                System.out.println(paddedLine);
            }
        }
        
        // Print separator between body and footer if footer exists
        if (footerLines != null && !footerLines.isEmpty()) {
            System.out.println(horizontalSeparator);
            
            // Print footer lines (no separators between footer lines)
            for (String line : footerLines) {
                int padding = maxWidth - line.length();
                String paddedLine = "│ " + line + " ".repeat(padding) + " │";
                System.out.println(paddedLine);
            }
        }
        
        // Print bottom border
        System.out.println(horizontalFooter);
    }

    /**
     * Prints text lines inside a bordered box with a header and body section (no footer).
     *
     * @param header the header text
     * @param bodyLines the lines of text to print in the body
     */
    public static void printBoxed(String header, List<String> bodyLines) {
        printBoxed(header, bodyLines, null);
    }

    /**
     * Converts a map to a list of formatted string lines.
     * Uses the default format: "key: value"
     *
     * @param map the map to convert
     * @return list of formatted string lines
     */
    public static <K, V> List<String> mapToLines(Map<K, V> map) {
        return mapToLines(map, (entry) -> String.format("%s: %s", entry.getKey(), entry.getValue()));
    }

    /**
     * Converts a map to a list of formatted string lines using a custom formatter.
     *
     * @param map the map to convert
     * @param formatter the function to format each map entry
     * @return list of formatted string lines
     */
    public static <K, V> List<String> mapToLines(Map<K, V> map, MapEntryFormatter<K, V, String> formatter) {
        return map.entrySet().stream()
            .map(formatter)
            .collect(Collectors.toList());
    }
}


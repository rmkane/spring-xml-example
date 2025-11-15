package org.example.util;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for formatting text in bordered boxes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoxFormatter {

    /**
     * Prints text lines inside a bordered box with a header and body section.
     *
     * @param header the header text
     * @param bodyLines the lines of text to print in the body
     */
    public static void printBoxed(String header, List<String> bodyLines) {
        // Find the longest line (header or body) to determine box width
        int headerWidth = header != null ? header.length() : 0;
        int maxBodyWidth = bodyLines.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0);
        int maxWidth = Math.max(headerWidth, maxBodyWidth);
        
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
            if (!bodyLines.isEmpty()) {
                System.out.println(horizontalSeparator);
            }
        }
        
        // Print body lines (no separators between body lines)
        for (String line : bodyLines) {
            int padding = maxWidth - line.length();
            String paddedLine = "│ " + line + " ".repeat(padding) + " │";
            System.out.println(paddedLine);
        }
        
        // Print bottom border
        System.out.println(horizontalFooter);
    }
}


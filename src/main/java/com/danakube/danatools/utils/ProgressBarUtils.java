package com.danakube.danatools.utils;

import com.danakube.danatools.DanaTools;
import org.bukkit.configuration.file.FileConfiguration;

public class ProgressBarUtils {

    public static String generateProgressBar(int currentXp, int maxXp) {
        FileConfiguration config = DanaTools.getInstance().getConfig();
        int length = config.getInt("progress-bar.length", 10);
        String symbol = config.getString("progress-bar.symbol", "■");
        String colorFilled = config.getString("progress-bar.color-filled", "<gradient:#2ecc71:#a3cb38>");
        String colorEmpty = config.getString("progress-bar.color-empty", "<gray>");
        boolean showPercentInside = config.getBoolean("progress-bar.show-percentage-inside", false);

        double progressRatio = maxXp > 0 ? (double) currentXp / maxXp : 0.0;
        progressRatio = Math.max(0.0, Math.min(1.0, progressRatio));

        int filledSegments = (int) Math.round(progressRatio * length);
        int emptySegments = length - filledSegments;

        if (showPercentInside) {
            int percent = maxXp > 0 ? (int) Math.round(progressRatio * 100) : 0;
            percent = Math.max(0, Math.min(100, percent));
            String percentStr = percent + "%";

            int leftCount = length / 2;
            int rightCount = length - leftCount;

            StringBuilder result = new StringBuilder();

            if (filledSegments <= leftCount) {
                int leftFilled = filledSegments;
                int leftEmpty = leftCount - leftFilled;

                if (leftFilled > 0) {
                    result.append(wrapWithColor(repeatSymbol(symbol, leftFilled), colorFilled));
                }

                StringBuilder emptyPartBuilder = new StringBuilder();
                if (leftEmpty > 0) {
                    emptyPartBuilder.append(repeatSymbol(symbol, leftEmpty));
                }
                emptyPartBuilder.append(percentStr);
                if (rightCount > 0) {
                    emptyPartBuilder.append(repeatSymbol(symbol, rightCount));
                }
                result.append(wrapWithColor(emptyPartBuilder.toString(), colorEmpty));
            } else {
                int rightFilled = filledSegments - leftCount;
                int rightEmpty = rightCount - rightFilled;

                StringBuilder filledPartBuilder = new StringBuilder();
                if (leftCount > 0) {
                    filledPartBuilder.append(repeatSymbol(symbol, leftCount));
                }
                filledPartBuilder.append(percentStr);
                if (rightFilled > 0) {
                    filledPartBuilder.append(repeatSymbol(symbol, rightFilled));
                }
                result.append(wrapWithColor(filledPartBuilder.toString(), colorFilled));

                if (rightEmpty > 0) {
                    result.append(wrapWithColor(repeatSymbol(symbol, rightEmpty), colorEmpty));
                }
            }

            return result.toString();
        } else {
            StringBuilder result = new StringBuilder();
            if (filledSegments > 0) {
                result.append(wrapWithColor(repeatSymbol(symbol, filledSegments), colorFilled));
            }
            if (emptySegments > 0) {
                result.append(wrapWithColor(repeatSymbol(symbol, emptySegments), colorEmpty));
            }
            return result.toString();
        }
    }

    private static String wrapWithColor(String text, String colorTag) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(colorTag);
        sb.append(text);
        if (colorTag.startsWith("<gradient") || colorTag.startsWith("<color")) {
            if (colorTag.startsWith("<gradient")) {
                sb.append("</gradient>");
            } else {
                sb.append("</color>");
            }
        }
        return sb.toString();
    }

    private static String repeatSymbol(String symbol, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }
}

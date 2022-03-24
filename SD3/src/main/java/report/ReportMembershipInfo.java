package report;

import java.util.HashMap;
import java.util.Map;

public class ReportMembershipInfo {
    private final Map<Integer, Integer> dayStatistics = new HashMap<>();
    private int visitedTimes = 0;
    private int timeInGym = 0;
    private int lastEnter = -1;

    public void addEnter(final int timeInSeconds) {
        visitedTimes++;
        lastEnter = timeInSeconds;
        addDay(timeInSeconds / 86400);
    }

    public void addExit(final int timeInSeconds) {
        if (lastEnter != -1) {
            timeInGym += timeInSeconds - lastEnter;
        }
        lastEnter = -1;
    }

    private void addDay(final int day) {
        dayStatistics.put(day, dayStatistics.getOrDefault(day, 0) + 1);
    }

    public String getStatistics() {
        final StringBuilder result = new StringBuilder();
        for (final int day : dayStatistics.keySet()) {
            result.append("On day ").append(day).append(" member visited ").append(dayStatistics.get(day)).append(" times").append(System.lineSeparator());
        }
        result.append("Visited ").append(visitedTimes).append(" times, summary time: ").append(timeInGym);
        return result.toString();
    }
}

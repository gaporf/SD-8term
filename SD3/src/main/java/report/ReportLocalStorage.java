package report;

import server.ServerConfig;
import server.ServerUtils;

import java.util.HashMap;
import java.util.Map;

public class ReportLocalStorage {
    private final Map<Integer, String> membershipNames = new HashMap<>();
    private final Map<Integer, ReportMembershipInfo> membershipInfoMap = new HashMap<>();

    public ReportLocalStorage() {
    }

    public ReportLocalStorage(final ServerConfig eventsConfig) {
        final String membershipsResult;
        try {
            membershipsResult = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                    "get_memberships" + "?" +
                    "password=" + eventsConfig.getPassword());
        } catch (final Exception e) {
            throw new ReportException("Can't connect to events store: " + e.getMessage(), e);
        }
        final String[] membershipResultLines = membershipsResult.split(System.lineSeparator());
        if (!membershipResultLines[0].equals("Info for memberships")) {
            throw new ReportException("Can't properly connect to event store");
        }
        for (int i = 1; i < membershipResultLines.length; i++) {
            final int id = Integer.parseInt(membershipResultLines[i].split("= ")[1].split(",")[0]);
            final String name = membershipResultLines[i].split("name = ")[1].split(",")[0];
            membershipNames.put(id, name);
        }

        for (final int membershipId : membershipNames.keySet()) {
            membershipInfoMap.put(membershipId, new ReportMembershipInfo());
            final String turnstileEventsResult;
            try {
                turnstileEventsResult = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                        "get_turnstile_events" + "?" +
                        "password=" + eventsConfig.getPassword() + "&" +
                        "membership_id=" + membershipId);
            } catch (final Exception e) {
                throw new ReportException("Can't connect to events store: " + e.getMessage(), e);
            }
            final String[] turnstileEventsResultLines = turnstileEventsResult.split(System.lineSeparator());
            if (!turnstileEventsResultLines[0].equals("Info for membership id = " + membershipId)) {
                throw new ReportException("Can't properly connect to event store");
            }
            for (int i = 2; i < turnstileEventsResultLines.length; i++) {
                int timeInSeconds = Integer.parseInt(turnstileEventsResultLines[i].split("time: ")[1].split(",")[0]);
                final String event = turnstileEventsResultLines[i].split("event ")[1];
                if (event.equals("ENTER")) {
                    membershipInfoMap.get(membershipId).addEnter(timeInSeconds);
                } else {
                    membershipInfoMap.get(membershipId).addExit(timeInSeconds);
                }
            }
        }
    }

    public String getStatistics(final int membershipId) {
        if (!membershipNames.containsKey(membershipId)) {
            throw new ReportException("Membership id = " + membershipId + " is not found");
        }
        final StringBuilder responseBuilder = new StringBuilder();
        final ReportMembershipInfo info = membershipInfoMap.get(membershipId);
        responseBuilder.append("Statistics for ").append(membershipNames.get(membershipId)).append(System.lineSeparator());
        responseBuilder.append(info.getStatistics()).append(System.lineSeparator());
        return responseBuilder.toString();
    }

    public void enter(final int membershipId, final int timeInSeconds) {
        membershipInfoMap.get(membershipId).addEnter(timeInSeconds);
    }

    public void exit(final int membershipId, final int timeInSeconds) {
        membershipInfoMap.get(membershipId).addExit(timeInSeconds);
    }

    public void addMembership(final int membershipId, final String name) {
        membershipNames.put(membershipId, name);
        membershipInfoMap.put(membershipId, new ReportMembershipInfo());
    }
}

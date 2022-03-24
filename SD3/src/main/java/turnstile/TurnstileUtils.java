package turnstile;

import server.ServerConfig;
import server.ServerUtils;

public class TurnstileUtils {
    public static int getLastEventId(final int membershipId, final ServerConfig eventsConfig) {
        final String turnstileEvents = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                "get_turnstile_events" + "?" +
                "password=" + eventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId);
        final String[] turnstileEventsLines = turnstileEvents.split(System.lineSeparator());
        if (!turnstileEventsLines[0].equals("Info for membership with id = " + membershipId)) {
            throw new TurnstileException(turnstileEvents);
        } else {
            return turnstileEventsLines.length - 1;
        }
    }

    public static void sendDataToEventsServer(final int membershipId, final int eventId, final String event, final ServerConfig eventsConfig) {
        final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                "new_turnstile_event" + "?" +
                "password=" + eventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "turnstile_event=" + event);
        if (!result.equals("Turnstile event: id = " + eventId + " for membership: id = " + membershipId + " is added")) {
            throw new TurnstileException("Can't send data to events server");
        }
    }
}

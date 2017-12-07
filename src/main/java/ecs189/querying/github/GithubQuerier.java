package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";
    private static final String PUSH_EVENT = "PushEvent";

    public static String eventsAsHTML(String user) throws IOException, ParseException
    {

        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");

        for (int i = 0; i < response.size(); i++)
        {

            JSONObject event = response.get(i);
            // Get event type
            String type = event.getString("type");
            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Add type of event as header
            sb.append("<h3 class=\"type\">");
            sb.append(type);
            sb.append("</h3>");

            // Add formatted date
            sb.append(" on ");
            sb.append(formatted);
            sb.append("<br />");

            // add commits with SHA (8 chars)
            sb.append("<a data-toggle=\"collapse\" href=\"#eventCommits-" + i + "\">Commits</a>");
            sb.append("<div id=eventCommits-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(getCommitsHTML(event));
            sb.append("</pre> </div>");
            sb.append("<br />");

            // Add collapsible JSON textbox
            // (don't worry about this for the homework;
            // it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException
    {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        String url = BASE_URL + user + "/events";
        System.out.println(url);
        JSONObject json = Util.queryAPI(new URL(url));
        System.out.println(json);
        JSONArray events = json.getJSONArray("root");

        int pushCount = 0;
        for (int i = 0; i < events.length() && pushCount < 10; i++)
        {
            JSONObject event = events.getJSONObject(i);
            String type = event.getString("type");
            if(type != null && type.equalsIgnoreCase(PUSH_EVENT))
            {
                eventList.add(events.getJSONObject(i));
                pushCount++;
            }
        }

        return eventList;
    }

    private static String getCommitsHTML(JSONObject event)
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            JSONObject payload = event.getJSONObject("payload");
            JSONArray commits = payload.getJSONArray("commits");

            sb.append("<table style = \"width:100%\" border=1 frame=void rules=rows>");

            sb.append("<tr>");
            sb.append("<th>Commit SHA</th>");
            sb.append("<th>Commit Author</th>");
            sb.append("<th>Commit Message</th>");
            sb.append("</tr>");

            for(int i = 0; i < commits.length(); i++)
            {
                JSONObject commit = commits.getJSONObject(i);
                String sha = commit.getString("sha").substring(0, 8); // first 8 characters
                String author = commit.getJSONObject("author").getString("name");
                String message = commit.getString("message"); // commit message

                sb.append("<tr>");

                sb.append("<td style = \"width:20%\">");
                sb.append(sha);
                sb.append("</td>");

                sb.append("<td style = \"width:30%\">");
                sb.append(author);
                sb.append("</td>");

                sb.append("<td style = \"width:50%\">");
                sb.append(message);
                sb.append("</td>");

                sb.append("</tr>");
            }

            sb.append("</table>");
        }
        catch(JSONException ex)
        {
            sb.append(ex.getMessage());
            Logger.getLogger(GithubQuerier.class.getName())
                  .log(Level.SEVERE, "Error retrieving commits", ex);
        }

        return sb.toString();
    }
}
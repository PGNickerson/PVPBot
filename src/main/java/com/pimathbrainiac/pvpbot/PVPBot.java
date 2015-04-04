package com.pimathbrainiac.pvpbot;

import java.io.*;
import java.net.*;
import java.util.*;
import org.pircbotx.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.goochjs.glicko2.*;

public class PVPBot extends ListenerAdapter
{
    static RatingCalculator ratingCalculator = new RatingCalculator(0.06, 0.5);
    static List<Rating> players = new ArrayList<>();
    static List<List<String>> matches = new ArrayList<>();
    final static long TIMEOUT_MILLIS = 120000;
    static List<String> accepted = new ArrayList<>();
    
    public PVPBot()
    {
    }

    void msg(MessageEvent event, String outputMessage)
    {
        event.getChannel().send().message(outputMessage);
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception
    {
        String message = event.getMessage();
        String nick = event.getUser().getNick();
        boolean isIrc = true;
        boolean isAction = false;
        boolean isMessage = false;
        if(nick.equalsIgnoreCase("PvPTest"))
        {
            isIrc = false;
            message = message.substring(4);
            if(message.startsWith("*"))
            {
                isAction = true;
                nick = message.substring(1, message.indexOf(' ') + 1);
            }
            else if(message.startsWith("["))
            {
                isMessage = true;
                nick = message.substring(1, message.indexOf(']'));
            }
            else
            {
                nick = message.substring(0, message.indexOf(' ') + 1);
            }
            message = message.substring(message.indexOf(' ') + 1);
        }
        if (message.equalsIgnoreCase(".test"))
        {
            msg(event, "test received");
        }
        if (message.toLowerCase().startsWith(".ping"))
        {
            msg(event, ping(message.substring(6)));
        }
        if(message.toLowerCase().startsWith("!challenge"))
        {
            boolean hasBusyPlayer = false;
            if(!matches.isEmpty())
            {
                for(List<String> match: matches)
                {
                    for(String player: match)
                    {
                        if((message.toLowerCase().contains(player.toLowerCase())) || (nick.equalsIgnoreCase(player)))
                        {
                            hasBusyPlayer = true;
                        }
                    }
                }
            }
            if(hasBusyPlayer)
            {
                msg(event, nick + ": One of the players challenged has either been challenged or is in a match.");
            }
            else
            {
                String[] challenged = message.split("\\s+");
                if(challenged.length > 2)
                {
                    List<String> match = new ArrayList<>();
                    match.add("brawl");
                    match.add("" + System.currentTimeMillis());
                    match.add(nick);
                    accepted.add(nick);
                    for(int i = 1; i < challenged.length; i++)
                    {
                        match.add(challenged[i]);
                    }
                    msg(event, "Brawl Created by " + match.get(2) + " with participants:");
                    for(int i = 3; i < match.size(); i++)
                    {
                        String participant = match.get(i);
                        msg(event, participant);
                    }
                    msg(event, "To accept, type \"!accept\" within the next 2 minutes.");
                    matches.add(match);
                }
                else if(challenged.length == 2)
                {
                    List<String> match = new ArrayList<>();
                    match.add("competitive");
                    match.add("" + System.currentTimeMillis());
                    match.add(nick);
                    accepted.add(nick);
                    match.add(challenged[1]);
                    msg(event, match.get(2) + " challenged " + match.get(3) + " to a competitive!");
                    msg(event, match.get(3) + ": To accept, type \"!accept\" within the next 2 minutes.");
                    matches.add(match);
                }
                else
                {
                    msg(event, "Nobody challenged.");
                }
            }
        }
        if(message.equalsIgnoreCase("!accept"))
        {
            boolean hasMatch = false;
            if(!accepted.contains(nick))
            {
                for(List<String> match : matches)
                {
                    if(match.contains(nick))
                    {
                        accepted.add(nick);
                        hasMatch = true;
                        msg(event, nick + ": You have accepted your challenge.");
                        int totalAccepted = 0;
                        for(String player : match)
                        {
                            if(accepted.contains(player))
                            {
                                totalAccepted++;
                            }
                        }
                        if(totalAccepted == (match.size() - 2))
                        {
                            msg(event, "Match Start!");
                        }
                    }
                }
                if(!hasMatch)
                {
                    msg(event, nick + ": You have no matches to accept.");
                }
            }
            else
            {
                msg(event, nick + ": You have already accepted.");
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        Configuration configuration = new Configuration.Builder().setName("PVPBot").setLogin("pimath").setAutoNickChange(true).setCapEnabled(true).addListener(new PVPBot()).setServerHostname("irc.mzima.net").addAutoJoinChannel("#PvPBot").buildConfiguration();
        PircBotX bot = new PircBotX(configuration);
        try
        {
            bot.startBot();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    static String ping(String url)
    {
        if (url.startsWith("http://"))
        {
        } else if (url.startsWith("https://"))
        {
            url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.
        } else
        {
            url = "http://" + url;
        }

        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            if ((200 <= responseCode && responseCode <= 399) || (responseCode == 403))
            {
                return "successful ping with code " + responseCode;
            }
            return "unsuccessful ping with code " + responseCode;

        } catch (IOException exception)
        {
            return "unsuccessful ping with IOException";
        }
    }
    
    static boolean searchRatings(String searchString)
    {
        boolean isFound = false;
        if(!players.isEmpty())
        {
            for(Rating player : players)
            {
                if (player.getUid().equalsIgnoreCase(searchString))
                {
                    isFound = true;
                }
            }
        }
        return isFound;
    }
}
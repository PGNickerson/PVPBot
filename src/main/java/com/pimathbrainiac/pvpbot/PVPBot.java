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
    static ArrayList<ArrayList<ArrayList<String>>> matches = new ArrayList<ArrayList<ArrayList<String>>>();
    final static long TIMEOUT_MILLIS = 120000;
    
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
        boolean action = false;
        if(nick.equals("PvPTest"))
        {
            isIrc = false;
            message = message.substring(4);
            if(message.startsWith("*"))
            {
                action = true;
                nick = message.substring(1, message.indexOf(' ') + 1);
            }
            else if(message.startsWith("["))
            {
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
            String[] challenged = message.split("\\s+");
            if(challenged.length > 2)
            {
                List<String> match = new ArrayList<>();
                match.add("brawl");
                match.add("" + System.currentTimeMillis());
                match.add(nick);
                for(int i = 1; i < challenged.length; i++)
                {
                    match.add(challenged[i]);
                }
                msg(event, "Brawl Created by " + match.get(2) + " at " + match.get(1) + " with participants ");
            }
            else if(challenged.length > 1)
            {
                List<String> match = new ArrayList<>();
                match.add("competitive");
                match.add("" + System.currentTimeMillis());
                match.add(nick);
                for(int i = 1; i < challenged.length; i++)
                {
                    match.add(challenged[i]);
                }
            }
            else
            {
                msg(event, "Nobody challenged");
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
    
    static boolean searchList(String searchString)
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
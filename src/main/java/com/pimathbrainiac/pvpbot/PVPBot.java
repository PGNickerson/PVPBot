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
    static List<String> deathStrings = new ArrayList<>();
    static List<BrawlPlayer> brawlPlayers = new ArrayList<>();
    
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
        boolean isQuit = false;
        boolean isMessage = false;
        if(nick.equalsIgnoreCase("PvPTest"))
        {
            isIrc = false;
            message = message.substring(4);
            if(message.startsWith("*"))
            {
                isQuit = message.equalsIgnoreCase("has left the room.");
                nick = message.substring(1, message.indexOf(' ') + 1);
            }
            else if(message.startsWith("["))
            {
                isMessage = true;
                nick = message.substring(1, message.indexOf(']'));
            }
            else
            {
                nick = message.substring(0, message.indexOf(' '));
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
                    
                    boolean isInBrawlPlayers = false;
                    if(!brawlPlayers.isEmpty())
                    {
                        for(BrawlPlayer player : brawlPlayers)
                        {
                            if(player.getName().equalsIgnoreCase(nick))
                            {
                                isInBrawlPlayers = true;
                            }
                        }
                    }
                    
                    if(!isInBrawlPlayers)
                    {
                        brawlPlayers.add(new BrawlPlayer(0, nick));
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
                    
                    boolean isInPlayers = false;
                    if(!players.isEmpty())
                    {
                        for(Rating player : players)
                        {
                            if(player.getUid().equalsIgnoreCase(nick))
                            {
                                isInPlayers = true;
                            }
                        }
                    }
                    
                    if(!isInPlayers)
                    {
                        players.add(new Rating(nick, ratingCalculator));
                    }
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
                        if(match.get(0).equalsIgnoreCase("brawl"))
                        {
                            boolean isInBrawlPlayers = false;
                            if(!brawlPlayers.isEmpty())
                            {
                                for(BrawlPlayer player : brawlPlayers)
                                {
                                    if(player.getName().equalsIgnoreCase(nick))
                                    {
                                        isInBrawlPlayers = true;
                                    }
                                }
                            }
                            
                            if(!isInBrawlPlayers)
                            {
                                brawlPlayers.add(new BrawlPlayer(0, nick));
                            }
                        }
                        
                        if(match.get(0).equalsIgnoreCase("competitive"))
                        {
                            boolean isInPlayers = false;
                            if(!players.isEmpty())
                            {
                                for(Rating player : players)
                                {
                                    if(player.getUid().equalsIgnoreCase(nick))
                                    {
                                        isInPlayers = true;
                                    }
                                }
                            }
                            
                            if(!isInPlayers)
                            {
                                players.add(new Rating(nick, ratingCalculator));
                            }
                        }
                        
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
        if(!isIrc && !isMessage && !isQuit)
        {
            for(String deathString : deathStrings)
            {
                if(message.toLowerCase().contains(deathString.toLowerCase()))
                {
                    for(List<String> match : matches)
                    {
                        if(match.contains(nick))
                        {
                            if(match.get(0).equalsIgnoreCase("brawl"))
                            {
                                if(accepted.contains(nick))
                                {
                                    accepted.remove(nick);
                                }
                                match.remove(nick);
                                
                                for(String killer : match)
                                {
                                    if(message.toLowerCase().contains(killer.toLowerCase()))
                                    {
                                        for(BrawlPlayer player : brawlPlayers)
                                        {
                                            if(player.getName().equalsIgnoreCase(killer))
                                            {
                                                player.setScore(player.getScore() + 1);
                                            }
                                        }
                                    }
                                }
                                
                                if(match.size() == 3)
                                {
                                    for(BrawlPlayer player : brawlPlayers)
                                    {
                                        if(player.getName().equalsIgnoreCase(match.get(2)))
                                        {
                                            player.setScore(player.getScore() + 2);
                                        }
                                    }
                                    msg(event, match.get(2) + " is the last one standing in their brawl!");
                                    
                                    if(accepted.contains(match.get(2)))
                                    {
                                        accepted.remove(match.get(2));
                                    }
                                    match.remove(2);
                                    matches.remove(match);
                                    break;
                                }
                            }
                            else
                            {
                                if(accepted.contains(nick))
                                {
                                    accepted.remove(nick);
                                }
                                //replace this line with logic for Glicko
                                match.remove(nick); //not this line
                                msg(event, match.get(2) + " has won their competitive!");
                                
                                if(accepted.contains(match.get(2)))
                                {
                                    accepted.remove(match.get(2));
                                }
                                match.remove(2);
                                matches.remove(match);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(isQuit)
        {
            for(List<String> match : matches)
            {
                if(match.contains(nick))
                {
                    if(match.get(0).equalsIgnoreCase("brawl"))
                    {
                        if(accepted.contains(nick))
                        {
                            accepted.remove(nick);
                        }
                        match.remove(nick);
                        
                        for(String killer : match)
                        {
                            if(message.toLowerCase().contains(killer.toLowerCase()))
                            {
                                for(BrawlPlayer player : brawlPlayers)
                                {
                                    if(player.getName().equalsIgnoreCase(killer))
                                    {
                                        player.setScore(player.getScore() + 1);
                                    }
                                }
                            }
                        }
                        
                        if(match.size() == 3)
                        {
                            for(BrawlPlayer player : brawlPlayers)
                            {
                                if(player.getName().equalsIgnoreCase(match.get(2)))
                                {
                                    player.setScore(player.getScore() + 2);
                                }
                            }
                            msg(event, match.get(2) + " is the last one standing in their brawl!");
                            
                            if(accepted.contains(match.get(2)))
                            {
                                accepted.remove(match.get(2));
                            }
                            match.remove(2);
                            matches.remove(match);
                            break;
                        }
                    }
                    else
                    {
                        if(accepted.contains(nick))
                        {
                            accepted.remove(nick);
                        }
                        //replace this line with logic for Glicko
                        match.remove(nick); //not this line
                        msg(event, match.get(2) + " has won their competitive!");
                        
                        if(accepted.contains(match.get(2)))
                        {
                            accepted.remove(match.get(2));
                        }
                        match.remove(2);
                        matches.remove(match);
                        break;
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        deathStrings.add("was squashed by a falling anvil");
        deathStrings.add("was pricked to death");
        deathStrings.add("walked into a cactus while trying to escape");
        deathStrings.add("was shot by arrow");
        deathStrings.add("drowned");
        deathStrings.add("drowned whilst trying to escape");
        deathStrings.add("blew up");
        deathStrings.add("was blown up by");
        deathStrings.add("hit the ground too hard");
        deathStrings.add("fell from a high place");
        deathStrings.add("fell off a ladder");
        deathStrings.add("fell off some vines");
        deathStrings.add("fell out of the water");
        deathStrings.add("fell into a patch of fire");
        deathStrings.add("fell into a patch of cacti");
        deathStrings.add("was doomed to fall by");
        deathStrings.add("was shot off some vines by");
        deathStrings.add("was shot off a ladder by");
        deathStrings.add("was blown from a high place by");
        deathStrings.add("went up in flames");
        deathStrings.add("burned to death");
        deathStrings.add("was burnt to a crisp whilst fighting");
        deathStrings.add("walked into a fire whilst fighting");
        deathStrings.add("was slain by");
        deathStrings.add("was shot by");
        deathStrings.add("was fireballed by");
        deathStrings.add("was killed by");
        deathStrings.add("got finished off by");
        deathStrings.add("tried to swim in lava");
        deathStrings.add("tried to swim in lava while trying to escape");
        deathStrings.add("died");
        deathStrings.add("was struck by lightning");
        deathStrings.add("was squashed by a falling block");
        deathStrings.add("got finished off by");
        deathStrings.add("was killed by magic");
        deathStrings.add("starved to death");
        deathStrings.add("suffocated in a wall");
        deathStrings.add("was killed while trying to hurt");
        deathStrings.add("was pummeled by");
        deathStrings.add("fell out of the world");
        deathStrings.add("fell from a high place and fell out of the world");
        deathStrings.add("was knocked into the void by");
        deathStrings.add("withered away");
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

class BrawlPlayer
{
    int score;
    String name;
    
    public BrawlPlayer(int scoreIn, String nameIn)
    {
        score = scoreIn;
        name = nameIn;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getScore()
    {
        return score;
    }
    
    public void setScore(int scoreIn)
    {
        score = scoreIn;
    }
}
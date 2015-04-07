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
    static RatingPeriodResults results = new RatingPeriodResults();
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
        if(nick.equalsIgnoreCase("CemetecMC"))
        {
            isIrc = false;
            message = message.substring(4);
            System.out.println(message);
            if(message.startsWith("*"))
            {
                isQuit = message.toLowerCase().contains("has left the room.");
                nick = message.substring(4, message.indexOf(' ') - 1);
            }
            else if(message.charAt(1) == '[')
            {
                isMessage = true;
                nick = message.substring(2, message.indexOf(']') - 1);
                message = message.substring(message.indexOf(']') + 1);
            }
            else
            {
                nick = message.substring(0, message.indexOf(' '));
            }
            message = message.substring(message.indexOf(' ') + 1);
            System.out.println(nick + "," + message);
        }
        if(nick.equalsIgnoreCase("PvPTest"))
        {
            isIrc = false;
            message = message.substring(4);
            System.out.println(message);
            if(message.startsWith("*"))
            {
                isQuit = message.toLowerCase().contains("has left the room.");
                nick = message.substring(1, message.indexOf(' '));
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
            System.out.println(message);
        }
        if (message.equalsIgnoreCase(".test"))
        {
            msg(event, "test received");
        }
        if (message.toLowerCase().startsWith(".ping"))
        {
            msg(event, ping(message.substring(6)));
        }
        if (message.toLowerCase().startsWith("!help"))
        {
            msg(event, "See this topic: http://www.cemetech.net/forum/viewtopic.php?t=11415");
        }
        if(message.toLowerCase().startsWith("!challenge"))
        {
            boolean hasBusyPlayer = message.toLowerCase().contains(nick.toLowerCase());
            if(!matches.isEmpty())
            {
                for(List<String> match: matches)
                {
                    for(String player: match)
                    {
                        if(message.toLowerCase().contains(player.toLowerCase()))
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
                        brawlPlayers.add(new BrawlPlayer(nick, 0));
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
                                brawlPlayers.add(new BrawlPlayer(nick, 0));
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
        if(message.equalsIgnoreCase("!decline"))
        {
            if(!accepted.contains(nick))
            {
                for(List<String> match : matches)
                {
                    if(match.contains(nick))
                    {
                        for(String player : match)
                        {
                            if(accepted.contains(player))
                            {
                                accepted.remove(player);
                            }
                        }
                        matches.remove(match);
                        msg(event, nick + "denclines the match! Match canceled!");
                    }
                }
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
                                    
                                    FileWriter writer = new FileWriter("brawls.csv", false);
                                    for(BrawlPlayer player : brawlPlayers)
                                    {
                                        writer.write(player.getName() + ",");
                                        writer.write(player.getScore() + ",");
                                    }
                                    writer.flush();
                                    writer.close();
                                    break;
                                }
                            }
                            else
                            {
                                if(accepted.contains(nick))
                                {
                                    accepted.remove(nick);
                                }
                                match.remove(nick); //not this line
                                msg(event, match.get(2) + " has won their competitive!");
                                if(accepted.contains(match.get(2)))
                                {
                                    accepted.remove(match.get(2));
                                }
                                Rating winner = null;
                                Rating loser = null;
                                for(Rating searched : players)
                                {
                                    if(searched.getUid().equalsIgnoreCase(nick))
                                    {
                                        winner = searched;
                                    }
                                    if(searched.getUid().equalsIgnoreCase(match.get(2)))
                                    {
                                        loser = searched;
                                    }
                                }
                                if(winner != null && loser != null)
                                {
                                    results.addResult(winner, loser);
                                    ratingCalculator.updateRatings(results);
                                    System.out.println("Ratings updated");
                                    FileWriter writer = new FileWriter("ratings.csv", false);
                                    for(Rating player : players)
                                    {
                                        writer.write(player.getUid() + ",");
                                        writer.write(player.getRating() + ",");
                                        writer.write(player.getRatingDeviation() + ",");
                                    }
                                    System.out.println("csv updated");
                                    writer.flush();
                                    writer.close();
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
                            
                            FileWriter writer = new FileWriter("brawls.csv", false);
                            for(BrawlPlayer player : brawlPlayers)
                            {
                                writer.write(player.getName() + ",");
                                writer.write(player.getScore() + ",");
                            }
                            writer.flush();
                            writer.close();
                            break;
                        }
                    }
                    else
                    {
                        if(accepted.contains(nick))
                        {
                            accepted.remove(nick);
                        }
                        match.remove(nick); //not this line
                        msg(event, match.get(2) + " has won their competitive!");
                        Rating winner = null;
                        Rating loser = null;
                        for(Rating searched : players)
                        {
                            if(searched.getUid().equalsIgnoreCase(nick))
                            {
                                winner = searched;
                            }
                            if(searched.getUid().equalsIgnoreCase(match.get(2)))
                            {
                                loser = searched;
                            }
                        }
                        if(winner != null && loser != null)
                        {
                            results.addResult(winner, loser);
                            ratingCalculator.updateRatings(results);
                            FileWriter writer = new FileWriter("ratings.csv", false);
                            for(Rating player : players)
                            {
                                writer.write(player.getUid() + ",");
                                writer.write(player.getRating() + ",");
                                writer.write(player.getRatingDeviation() + ",");
                            }
                            writer.flush();
                            writer.close();
                        }
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
        File ratings = new File("ratings.csv");
        if(!ratings.exists())
        {
            ratings.createNewFile();
        }
        Scanner sc1 = new Scanner(new File("ratings.csv"));
        sc1.useDelimiter(",");
        
        while(sc1.hasNext())
        {
            players.add(new Rating(sc1.next(), ratingCalculator, sc1.nextDouble(), sc1.nextDouble(), 0.06));
        }
        
        File brawlRatings = new File("brawls.csv");
        if(!brawlRatings.exists())
        {
            brawlRatings.createNewFile();
        }
        
        Scanner sc2 = new Scanner(new File("brawls.csv"));
        sc2.useDelimiter(",");
        
        while(sc2.hasNext())
        {
            brawlPlayers.add(new BrawlPlayer(sc2.next(), sc2.nextInt()));
        }
        Configuration configuration = new Configuration.Builder().setName("PVPBot").setLogin("PvPBot").setAutoNickChange(true).setCapEnabled(true).addListener(new PVPBot()).setServerHostname("irc.mzima.net").addAutoJoinChannel("#cemetech-mc").addAutoJoinChannel("#PvPBot").buildConfiguration();
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
}

class BrawlPlayer
{
    int score;
    String name;
    
    public BrawlPlayer(String nameIn, int scoreIn)
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
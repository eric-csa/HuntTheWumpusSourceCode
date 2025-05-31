package Trivia;

import Messages.Messages;
import ServerData.ServerData;
import Titles.TitleColors;
import Titles.TitleSender;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

// the trivia class is responsible for managing the trivia for a certain player.
public class Trivia implements Listener {
    // hard-coded trivia questions created by Arnav
    static String[][] triviaQuestions = {
            {"What is the capital of France?", "Berlin", "London", "Paris", "Madrid", "3"},
            {"Which planet is known as the Red Planet?", "Earth", "Mars", "Jupiter", "Saturn", "2"},
            {"Who wrote 'Romeo and Juliet'?", "William Shakespeare", "Charles Dickens", "Mark Twain", "Jane Austen", "1"},
            {"What is the largest mammal?", "Elephant", "Blue Whale", "Giraffe", "Hippopotamus", "2"},
            {"In which year did the Titanic sink?", "1912", "1905", "1920", "1918", "1"},
            {"What is the chemical symbol for water?", "H2O", "CO2", "O2", "NaCl", "1"},
            {"Who painted the Mona Lisa?", "Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Michelangelo", "3"},
            {"What is the smallest prime number?", "1", "2", "3", "5", "2"},
            {"Which continent is the Sahara Desert located on?", "Asia", "Africa", "Australia", "South America", "2"},
            {"What is the hardest natural substance on Earth?", "Gold", "Iron", "Diamond", "Quartz", "3"},
            {"Who is known as the father of computers?", "Bill Gates", "Charles Babbage", "Steve Jobs", "Alan Turing", "2"},
            {"Which element has the atomic number 6?", "Carbon", "Oxygen", "Nitrogen", "Helium", "1"},
            {"What is the currency of Japan?", "Yuan", "Dollar", "Yen", "Won", "3"},
            {"Who discovered penicillin?", "Alexander Fleming", "Marie Curie", "Isaac Newton", "Albert Einstein", "1"},
            {"How many continents are there on Earth?", "5", "6", "7", "8", "3"},
            {"What is the largest planet in our Solar System?", "Saturn", "Earth", "Jupiter", "Neptune", "3"},
            {"Which language is primarily spoken in Brazil?", "Spanish", "Portuguese", "English", "French", "2"},
            {"What is the boiling point of water at sea level in Celsius?", "90째C", "100째C", "110째C", "120째C", "2"},
            {"Who wrote the novel '1984'?", "George Orwell", "Aldous Huxley", "F. Scott Fitzgerald", "Ernest Hemingway", "1"},
            {"Which gas do plants absorb from the atmosphere during photosynthesis?", "Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen", "2"}
    };
    // minCorrect is the minimum number of answers needed to pass the trivia.
    private int minCorrect = 0;
    private int correct; // current questions the player has got correct
    private int total; // total questions the player should be given
    private int solved = 0; // how many questions the player has currently answered
    private int currentQuestion = (int) (Math.random()*triviaQuestions.length);
    private Player player;
    private BukkitTask triviaChecker;
    private boolean isCheckingQuestion;
    private boolean[] isQuestionUsed;
    public Trivia (int correct, int total, Player player) {
        this.minCorrect = correct;
        this.total = total;

        isQuestionUsed = new boolean[triviaQuestions.length];
        Component message = Component.text("You stumbled into a hazard and need to solve " + minCorrect + " out of " + this.total + " questions correct in order earn an extra life.").color(NamedTextColor.WHITE);
        player.sendMessage(message);
        currentQuestion = (int) (Math.random() * triviaQuestions.length);
        isQuestionUsed[currentQuestion] = true;
        Component tText = Component.text("Question "+(solved+1)+": "+triviaQuestions[currentQuestion][0]).color(NamedTextColor.LIGHT_PURPLE);
        player.sendMessage(tText);
        player.sendMessage("A) "+triviaQuestions[currentQuestion][1]);
        player.sendMessage("B) "+triviaQuestions[currentQuestion][2]);
        player.sendMessage("C) "+triviaQuestions[currentQuestion][3]);
        player.sendMessage("D) "+triviaQuestions[currentQuestion][4]);
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("HuntTheWumpusPlugin"));
        this.player = player;
        isCheckingQuestion = false;
        startTriviaChecker();

    }
    // the trivia checker. The player answers trivia by stepping on 4 regions, called A, B, C, and D. That is detected with different types
    // of minecraft wool.
    public void startTriviaChecker () {
        triviaChecker = new BukkitRunnable() {
            @Override
            public void run () {
                if (player.isOnline()) {
                    Player upatedPlayer = Bukkit.getPlayer(player.getName());
                    if (upatedPlayer != player) {
                        player = upatedPlayer;
                        player.sendMessage(Component.text("Question "+(solved+1)+": "+triviaQuestions[currentQuestion][0]).color(NamedTextColor.LIGHT_PURPLE));
                        player.sendMessage("A) "+triviaQuestions[currentQuestion][1]);
                        player.sendMessage("B) "+triviaQuestions[currentQuestion][2]);
                        player.sendMessage("C) "+triviaQuestions[currentQuestion][3]);
                        player.sendMessage("D) "+triviaQuestions[currentQuestion][4]);
                    }
                }
                if (player == null) {
                    triviaChecker.cancel();
                }
                if (ServerData.getPlayerData(player).getGame() == null) {
                    triviaChecker.cancel();
                }
                if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == null) {
                    triviaChecker.cancel();
                }
                if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.WHITE_WOOL) {
                    isCheckingQuestion = false;
                }
                if (ServerData.getPlayerData(player).getGame() != null && !isCheckingQuestion) {
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.RED_WOOL) {
                        checkQuestion(1);
                    }
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.YELLOW_WOOL) {
                        checkQuestion(2);
                    }
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.BLUE_WOOL) {
                        checkQuestion(3);
                    }
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.GREEN_WOOL) {
                        checkQuestion(4);
                    }

                }
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    // destroys the runTasks and event listeners.
    public void destroy () {
        triviaChecker.cancel();
        HandlerList.unregisterAll(this);
    }
    // kills the player upon failing the trivia.
    public void killPlayer () {
        TitleSender.sendTitle(Messages.deathMessage, "you failed the trivia", player, 20, 60, 20,
                TitleColors.playerDeathTitleColor, TitleColors.playerDeathSubtitleColor);
        player.performCommand("kill");
    }
    // checks if the player's answer is correct, and gives a new question.
    public void checkQuestion(int triviaAnswer) {
        //int triviaAnswer = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(event.message()));
        isCheckingQuestion = true;
        System.out.println(triviaAnswer);
        solved++;
        if(triviaAnswer == Integer.parseInt(triviaQuestions[currentQuestion][5])){
            correct++;
            Component cor = Component.text("Correct!").color(NamedTextColor.GREEN);
            player.sendMessage(cor);
        }
        else{
            Component cor = Component.text("Wrong!").color(NamedTextColor.RED);
            player.sendMessage(cor);
        }
        if(correct >= minCorrect){
            player.sendMessage("Congrats! You survived the trivia!");
            ServerData.getPlayerData(player).getGame().movePlayerAfterTrivia();
            destroy();
            return;
        }
        else if(solved >= total){
            Component cor = Component.text("You failed to solve enough questions. Game Over").color(NamedTextColor.RED);
            player.sendMessage(cor);
            killPlayer();
            destroy();
            return;
        }

        do {
            currentQuestion = (int) (Math.random() * triviaQuestions.length);
        } while (isQuestionUsed[currentQuestion]);

        Component tText = Component.text("Question "+(solved+1)+": "+triviaQuestions[currentQuestion][0]).color(NamedTextColor.LIGHT_PURPLE);
        player.sendMessage(tText);
        player.sendMessage("A) "+triviaQuestions[currentQuestion][1]);
        player.sendMessage("B) "+triviaQuestions[currentQuestion][2]);
        player.sendMessage("C) "+triviaQuestions[currentQuestion][3]);
        player.sendMessage("D) "+triviaQuestions[currentQuestion][4]);
        ServerData.getPlayerData(player).getGame().getCave().renderRoom(player);

        isQuestionUsed[currentQuestion] = true;
    }
}

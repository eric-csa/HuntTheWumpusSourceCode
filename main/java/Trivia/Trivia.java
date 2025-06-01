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

// The trivia class is responsible for managing trivia questions and answers for a single player.
public class Trivia implements Listener {
    /* Hard-coded trivia questions: Each question has text, four answer options, and the index of the correct answer (as string)                                                                                                                        */ static String[][] triviaQuestions = {{"What is the capital of France?", "Berlin", "London", "Paris", "Madrid", "3"},{"Which planet is known as the Red Planet?", "Earth", "Mars", "Jupiter", "Saturn", "2"},{"Who wrote 'Romeo and Juliet'?", "William Shakespeare", "Charles Dickens", "Mark Twain", "Jane Austen", "1"},{"What is the largest mammal?", "Elephant", "Blue Whale", "Giraffe", "Hippopotamus", "2"},{"In which year did the Titanic sink?", "1912", "1905", "1920", "1918", "1"},{"What is the chemical symbol for water?", "H2O", "CO2", "O2", "NaCl", "1"},{"Who painted the Mona Lisa?", "Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Michelangelo", "3"},{"What is the smallest prime number?", "1", "2", "3", "5", "2"},{"Which continent is the Sahara Desert located on?", "Asia", "Africa", "Australia", "South America", "2"},{"What is the hardest natural substance on Earth?", "Gold", "Iron", "Diamond", "Quartz", "3"},{"Who is known as the father of computers?", "Bill Gates", "Charles Babbage", "Steve Jobs", "Alan Turing", "2"},{"Which element has the atomic number 6?", "Carbon", "Oxygen", "Nitrogen", "Helium", "1"},{"What is the currency of Japan?", "Yuan", "Dollar", "Yen", "Won", "3"},{"Who discovered penicillin?", "Alexander Fleming", "Marie Curie", "Isaac Newton", "Albert Einstein", "1"},{"How many continents are there on Earth?", "5", "6", "7", "8", "3"},{"What is the largest planet in our Solar System?", "Saturn", "Earth", "Jupiter", "Neptune", "3"},{"Which language is primarily spoken in Brazil?", "Spanish", "Portuguese", "English", "French", "2"},{"What is the boiling point of water at sea level in Celsius?", "90째C", "100째C", "110째C", "120째C", "2"},{"Who wrote the novel '1984'?", "George Orwell", "Aldous Huxley", "F. Scott Fitzgerald", "Ernest Hemingway", "1"},{"Which gas do plants absorb from the atmosphere during photosynthesis?", "Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen", "2"}};
    // Scrapping logic from /triviaquestions.yaml

    private int minCorrect = 0; // Minimum correct answers needed to pass the trivia challenge
    private int correct;        // Number of questions the player has answered correctly so far
    private int total;          // Total number of trivia questions the player will be asked
    private int solved = 0;     // Number of questions the player has answered so far
    private int currentQuestion = (int) (Math.random()*triviaQuestions.length); // Index of the current trivia question

    private Player player;          // Player taking the trivia
    private BukkitTask triviaChecker;  // Scheduled task that repeatedly checks player's input/position for answers
    private boolean isCheckingQuestion; // Flag to prevent multiple checks for the same question
    private boolean[] isQuestionUsed;   // Tracks which questions have already been asked to avoid repeats

    // Constructor initializes trivia game for the player with the required number of correct answers and total questions
    public Trivia (int correct, int total, Player player) {
        this.minCorrect = correct;
        this.total = total;

        isQuestionUsed = new boolean[triviaQuestions.length]; // Initialize usage tracking array

        // Inform player about the trivia challenge requirements
        Component message = Component.text("You stumbled into a hazard and need to solve " + minCorrect + " out of " + this.total + " questions correct in order earn an extra life.").color(NamedTextColor.WHITE);
        player.sendMessage(message);

        // Select a random starting question and mark it as used
        currentQuestion = (int) (Math.random() * triviaQuestions.length);
        isQuestionUsed[currentQuestion] = true;

        // Send the first question and answer choices to the player
        Component tText = Component.text("Question "+(solved+1)+": "+triviaQuestions[currentQuestion][0]).color(NamedTextColor.LIGHT_PURPLE);
        player.sendMessage(tText);
        player.sendMessage("A) "+triviaQuestions[currentQuestion][1]);
        player.sendMessage("B) "+triviaQuestions[currentQuestion][2]);
        player.sendMessage("C) "+triviaQuestions[currentQuestion][3]);
        player.sendMessage("D) "+triviaQuestions[currentQuestion][4]);

        // Register this class as an event listener to respond to player interactions
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("HuntTheWumpusPlugin"));

        this.player = player;
        isCheckingQuestion = false;

        // Start the periodic checker that listens for player answers
        startTriviaChecker();
    }

    // The trivia checker periodically checks the player's position to detect which answer block they've stepped on.
    // Different wool colors represent answer choices A, B, C, and D.
    public void startTriviaChecker () {
        triviaChecker = new BukkitRunnable() {
            @Override
            public void run () {
                if (player.isOnline()) {
                    // Refresh player object to avoid stale references
                    Player upatedPlayer = Bukkit.getPlayer(player.getName());
                    if (upatedPlayer != player) {
                        player = upatedPlayer;
                        // Resend the current question and answers if player reference was updated
                        player.sendMessage(Component.text("Question "+(solved+1)+": "+triviaQuestions[currentQuestion][0]).color(NamedTextColor.LIGHT_PURPLE));
                        player.sendMessage("A) "+triviaQuestions[currentQuestion][1]);
                        player.sendMessage("B) "+triviaQuestions[currentQuestion][2]);
                        player.sendMessage("C) "+triviaQuestions[currentQuestion][3]);
                        player.sendMessage("D) "+triviaQuestions[currentQuestion][4]);
                    }
                }
                // Cancel trivia if player is no longer online or game context is lost
                if (player == null) {
                    triviaChecker.cancel();
                }
                if (ServerData.getPlayerData(player).getGame() == null) {
                    triviaChecker.cancel();
                }
                // Cancel if player block info is unavailable
                if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == null) {
                    triviaChecker.cancel();
                }
                // Reset checking flag when player stands on white wool (neutral block)
                if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.WHITE_WOOL) {
                    isCheckingQuestion = false;
                }
                // Only check answers if not already processing a question check
                if (ServerData.getPlayerData(player).getGame() != null && !isCheckingQuestion) {
                    // Check which colored wool player is standing on to determine answer
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.RED_WOOL) {
                        checkQuestion(1); // Answer A
                    }
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.YELLOW_WOOL) {
                        checkQuestion(2); // Answer B
                    }
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.BLUE_WOOL) {
                        checkQuestion(3); // Answer C
                    }
                    if (ServerData.getPlayerData(player).getGame().getPlayerBlock() == Material.GREEN_WOOL) {
                        checkQuestion(4); // Answer D
                    }
                }
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1); // Run task every tick
    }

    // Cleans up by canceling the trivia checker and unregistering event listeners
    public void destroy () {
        triviaChecker.cancel();
        HandlerList.unregisterAll(this);
    }

    // Kills the player in-game when they fail the trivia challenge
    public void killPlayer () {
        TitleSender.sendTitle(Messages.deathMessage, "you failed the trivia", player, 20, 60, 20,
                TitleColors.playerDeathTitleColor, TitleColors.playerDeathSubtitleColor);
        player.performCommand("kill"); // Executes the kill command on the player
    }

    // Checks the player's submitted answer against the correct answer, then updates state accordingly
    public void checkQuestion(int triviaAnswer) {
        isCheckingQuestion = true; // Lock to prevent repeated checking

        System.out.println(triviaAnswer); // Debug output of selected answer

        solved++; // Increment total questions answered

        // Check if answer is correct (compare to correct answer index stored in question)
        if(triviaAnswer == Integer.parseInt(triviaQuestions[currentQuestion][5])){
            correct++; // Increment correct answers count
            Component cor = Component.text("Correct!").color(NamedTextColor.GREEN);
            player.sendMessage(cor); // Inform player of correct answer
        }
        else{
            Component cor = Component.text("Wrong!").color(NamedTextColor.RED);
            player.sendMessage(cor); // Inform player of wrong answer
        }

        // Check if player has met minimum correct answers needed to pass
        if(correct >= minCorrect){
            player.sendMessage("Congrats! You survived the trivia!");
            ServerData.getPlayerData(player).getGame().movePlayerAfterTrivia(); // Move player out of trivia hazard
            destroy(); // Cleanup trivia game resources
            return;
        }
        // If player answered all questions but didn't reach minCorrect, they fail
        else if(solved >= total){
            Component cor = Component.text("You failed to solve enough questions. Game Over").color(NamedTextColor.RED);
            player.sendMessage(cor);
            killPlayer(); // Kill player in-game
            destroy();
            return;
        }

        // Select a new random question that has not been used yet
        do {
            currentQuestion = (int) (Math.random() * triviaQuestions.length);
        } while (isQuestionUsed[currentQuestion]);

        // Send the new question and answer choices to the player
        Component tText = Component.text("Question "+(solved+1)+": "+triviaQuestions[currentQuestion][0]).color(NamedTextColor.LIGHT_PURPLE);
        player.sendMessage(tText);
        player.sendMessage("A) "+triviaQuestions[currentQuestion][1]);
        player.sendMessage("B) "+triviaQuestions[currentQuestion][2]);
        player.sendMessage("C) "+triviaQuestions[currentQuestion][3]);
        player.sendMessage("D) "+triviaQuestions[currentQuestion][4]);

        // Render the player's current room (visual update in the game)
        ServerData.getPlayerData(player).getGame().getCave().renderRoom(player);

        // Mark this question as used to avoid repetition
        isQuestionUsed[currentQuestion] = true;
    }
}

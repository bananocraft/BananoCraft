package banano.bananominecraft.bananoeconomy;

import com.mongodb.client.*;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.net.URI;

import static com.mongodb.client.model.Filters.eq;


public class DB {


    static Plugin plugin = Main.getPlugin(Main.class);
    private static MongoClient mongoClient = MongoClients.create(getMongoURI());

    private static MongoDatabase db = mongoClient.getDatabase("BananoCraft");

    public static String getMongoURI(){
        return plugin.getConfig().getString("mongoURI");
    }

    public static MongoClient getMongoClient(){ return mongoClient; }

    private static URI getURI() throws Exception{
        return new URI(plugin.getConfig().getString("mongoURI"));
    }

    /**
     * @returns: null if player doesn't not exist.
     */
    public static String getWallet(Player player){

        if (accountExists(player))
            return getUserDBEntry(player).getString("wallet");

        return null;
        
    }

    public static void storeAccount(Player player, String banAccount){

        String playerUUID = player.getUniqueId().toString();

        Document document1 = new Document("_id", playerUUID)
                .append("name", player.getName())
                .append("wallet", banAccount)
                .append("frozen", false);
        db.getCollection("users").insertOne(document1);

    }

    public static boolean isFrozen(Player player){
        if (accountExists(player))
            return getUserDBEntry(player).getBoolean("frozen");

        return false;
    }

    public static boolean freezePlayer(Player player){
        if (accountExists(player)){
            if (isFrozen(player)){
                System.out.printf("%s already frozen", player.getName());
                return true;
            }else{
                return updateUserEntry(player, new Document("frozen", true));
            }
        }
        return false;
    }

    public static boolean unfreezePlayer(Player player){
        if (accountExists(player)){
            if (!isFrozen(player)){
                System.out.printf("%s already not frozen", player.getName());
                return true;
            }else{
                return updateUserEntry(player, new Document("frozen", false));
            }
        }
        return false;
    }

    public static boolean freezePlayer(String uname){
        FindIterable<Document> possibleOfflinePlayers = getUserDBEntry(uname);
        if (possibleOfflinePlayers.first() == null){
            return false;
        }
        db.getCollection("users").updateMany(eq("name", uname),
                new Document("$set", new Document("frozen",true)));
        return true;

    }
    public static boolean unfreezePlayer(String uname){
        FindIterable<Document> possibleOfflinePlayers = getUserDBEntry(uname);
        if (possibleOfflinePlayers.first() == null){
            return false;
        }
        db.getCollection("users").updateMany(eq("name", uname),
                new Document("$set", new Document("frozen",false)));
        return true;

    }

    public static boolean accountExists(Player player){

        Document user = getUserDBEntry(player);
        if (user == null){
            return false;
        }
        return true;
    }

    public static boolean updateUserEntry(Player player, Document update){
        String playerUUID = player.getUniqueId().toString();
        db.getCollection("users").updateOne(eq("_id", playerUUID),
                                  new Document("$set", update));
        return true;
    }

    /**
     * @returns: null if player doesn't not exist.
     */
    public static Document getUserDBEntry(Player player){

        String playerUUID = player.getUniqueId().toString();
        Document query = new Document("_id",playerUUID);
        Document user = db.getCollection("users").find(query).first();

        return user;
    }
    public static FindIterable<Document> getUserDBEntry(String name){
        Document query = new Document("name",name);
        FindIterable<Document> user = db.getCollection("users").find(query);
        return user;
    }

}

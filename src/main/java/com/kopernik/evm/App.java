package com.kopernik.evm;

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * Hello world!
 */
public class App {

  private static Logger log = LoggerFactory.getLogger(App.class);
//    private static final String nodeUrl = System.getenv().getOrDefault("WEB3J_NODE_URL", "http://localhost:8545");

  // To be used later
//    private static final String walletPassword = System.getenv().getOrDefault("WEB3J_WALLET_PASSWORD", "password");
//    private static final String walletPath = System.getenv().getOrDefault("WEB3J_WALLET_PATH", "./wallet.json");

  public static void main(String[] args) throws Exception {

// Load properties

    Properties props = null;
    try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {

      props = new Properties();

      if (input == null) {
        System.out.println("Sorry, unable to find config.properties");
        return;
      }

      //load a properties file from class path, inside static method
      props.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    Credentials credentials = Credentials.create(props.getProperty("PRIVATE_KEY"));
    String nodeUrl = props.getProperty("WEB3J_NODE_URL");

// Connection to the node
    WebSocketService web3jService = new WebSocketService(nodeUrl, false);
    web3jService.connect();
    Web3j web3j = Web3j.build(web3jService);

    String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
    System.out.println(String.format("Connected to Ethereum node %s : %s", nodeUrl, clientVersion));

// Load Dai contract
    String daiContractAddress = "0x6B175474E89094C44Da98b954EedeAC495271d0F";
    ERC20 daiToken = ERC20.load(daiContractAddress, web3j, credentials, new DefaultGasProvider());

    String symbol = daiToken.symbol().send();
    String name = daiToken.name().send();
    BigInteger decimal = daiToken.decimals().send();

// Subscribe to blocks
    web3j.blockFlowable(false)
        .blockingSubscribe(block -> {
          System.out.println("NEW BLOCK -> " + block.getBlock().getNumber().intValue());
          System.out.println("symbol: " + symbol);
          System.out.println("name: " + name);
          System.out.println("decimal: " + decimal.intValueExact());
          String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
          System.out.println("Time: " + timeStamp);
        });

  }
}

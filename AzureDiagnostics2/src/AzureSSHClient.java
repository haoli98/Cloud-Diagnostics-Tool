import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class AzureSSHClient {
  private static final String REDIS_HOST = "40.87.94.171";

  private static final String command1 = "free -m | awk \'NR==2{printf \"Memory Usage: %s/%sMB (%.2f%%)\", $3,$2,$3*100/$2 }\' ";
  private static final String command2 = "df -h | awk \'$NF==\"/\"{printf \"Disk Usage: %d/%dGB (%s)\", $3,$2,$5}\'";
  private static final String command3 = "ps -A -o %cpu | awk \'{s+=$1} END {print s \"%\"}\'";

  private Jedis _jedis;
  private List<String> _users;
  private List<String> _commands;
  private Map<String, List<String>> _userIdServerMap;

  public AzureSSHClient(List<String> users, Map<String, List<String>> userIdServerMap) throws IOException {
    this._userIdServerMap = userIdServerMap;
    this._users = users;
    this._jedis = new Jedis(REDIS_HOST);
    this._commands = Arrays.asList(command1, command2, command3);
  }

  public void startAzureSSHClient() throws JSchException, IOException, InterruptedException {
    while (true) {
      for (String user : this._users) {
        System.out.println("Starting azure client for user: " + user);

        List<String> servers = this._userIdServerMap.get(user);

        for (String server : servers) {
          this.runCommand(user, server);
        }
      }
      TimeUnit.SECONDS.sleep(5);
    }
  }

  private void runCommand(String user, String server) throws JSchException, IOException {
    Session session = setupSshSession(user, server);

    try {
      session.connect();
    } catch (Exception e) {
      System.out.println(e);
      System.out.println("Private Key may have been misconfigured");
      return;
    }

    boolean flag = true;
    for (String command : this._commands) {
      Channel channel = session.openChannel("exec");

      ((ChannelExec) channel).setCommand(command);
      channel.setInputStream(null);
      ((ChannelExec) channel).setErrStream(System.err);

      InputStream in = channel.getInputStream();
      channel.connect();

      byte[] tmp = new byte[1024];
      while (flag) {
        while (in.available() > 0) {
          int i = in.read(tmp, 0, 1024);
          if (i <= 0)
            break;

          String data = new String(tmp, 0, i);
          this._jedis.publish(user, data);
          System.out.println(data);
          flag = false;
        }
      }
      flag = true;
      channel.disconnect();
    }
    session.disconnect();

  }

  private Session setupSshSession(String userName, String server) throws JSchException {
    try {
      System.out.println("Setting up session for: " + server);
      JSch jsch = new JSch();

      String privateKeyPath = "../../users/" + userName + "/keys/" + server + ".txt";
      jsch.addIdentity(privateKeyPath);

      String host = userName + "@" + server;
      String user = host.substring(0, host.indexOf('@'));
      host = host.substring(host.indexOf('@') + 1);

      Session session = jsch.getSession(user, host, 22);
      session.setConfig("StrictHostKeyChecking", "no"); // disable check for RSA key
      return session;

    } catch (Exception e) {
      System.out.println(e);
      return null;
    }
  }
}
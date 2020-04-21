import com.google.common.io.CharStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

class RunCommandViaSsh {

    private static final String SSH_HOST = "40.121.57.154";
    private static final String SSH_LOGIN = "hl677";
    private static final String SSH_PASSWORD = "";
    private static final String PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDM6C4UvkvSDwH6JHdp2/dp6cCMkgfws30oMnlPklENS0UtHW5QI/AOt63VEbcsBmiZqbWbXANBnuh4kGPJEcJU7n4fpOxPnm7rP5AVj9jixGUIhixiYxcg5SDYWQzaApLPR7HvTG+rps8+nP1NX7hrvGNdOoi65ngJG5ATORsWLu+N1OQTfws4KUrOEUNF373/B7b7LTlxXxoIV9sGkgFA45GFda/IJwMUELi9P9woDY50CHP2DMgi2PC7yTS5FxIJd9OXRuuRhlpAKotD2o2kjr6vwzJsO5o4IwBR4szCpqHusFP6Sa/LZoimTqA6znfnkppgjsu8fx4iFgNqLGdh hao@cc-1705de86-5b556b8b46-gdbzj";
    static String privateKey = ".ssh/id_rsa";
    
    public static void main() throws JSchException {
        System.out.println(runCommand("pwd"));
        System.out.println(runCommand("ls -la"));
    }

    private static List<String> runCommand(String command) throws JSchException {
        Session session = setupSshSession();
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        try {
            channel.setCommand("command");
            channel.setInputStream(null);
            InputStream output = channel.getInputStream();
            channel.connect();

            String result = CharStreams.toString(new InputStreamReader(output));
            return asList(result.split("\n"));

        } catch (JSchException | IOException e) {
            closeConnection(channel, session);
            throw new RuntimeException(e);

        } finally {
            closeConnection(channel, session);
        }
    }

    private static Session setupSshSession() throws JSchException {
    	JSch jsch = new JSch();
    	jsch.addIdentity(privateKey);
        Session session = new JSch().getSession(SSH_LOGIN, SSH_HOST, 22);
        session.setPassword(SSH_PASSWORD);
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig("StrictHostKeyChecking", "no"); // disable check for RSA key
        return session;
    }

    private static void closeConnection(ChannelExec channel, Session session) {
        try {
            channel.disconnect();
        } catch (Exception ignored) {
        }
        session.disconnect();
    }
}
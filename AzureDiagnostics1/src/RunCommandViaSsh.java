import com.google.common.io.CharStreams;
import com.jcraft.jsch.Channel;
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
    static String privateKey = "/Users/haoli/.ssh/id_rsa";
    static String command1 = "pwd";
    
    public static void main() throws JSchException, IOException {
      runCommand("pwd");
       //System.out.println(runCommand("ls -la"));
    	//setupSshSession();
    }

    private static void runCommand(String command) throws JSchException, IOException {
        Session session = setupSshSession();
        session.connect();
        System.out.println(session.isConnected());
        
//        Channel channel = session.openChannel("shell");
////      Channel channel=session.openChannel("shell");
////
//      channel.setInputStream(System.in);
//      channel.setOutputStream(System.out);
//      System.out.println(channel.getOutputStream());
//      channel.connect();
        Channel channel=session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command1);
        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);
        
        InputStream in=channel.getInputStream();
        channel.connect();
        byte[] tmp=new byte[1024];
        while(true){
          while(in.available()>0){
            int i=in.read(tmp, 0, 1024);
            if(i<0)break;
            System.out.print(new String(tmp, 0, i));
          }

    }
    }
    private static Session setupSshSession() throws JSchException {
    	try{
        	String privateKey = "/Users/haoli/.ssh/id_rsa";
            JSch jsch=new JSch();
      		jsch.addIdentity(privateKey);

//          JFileChooser chooser = new JFileChooser();
//          chooser.setDialogTitle("Choose your privatekey(ex. ~/.ssh/id_dsa)");
//          chooser.setFileHidingEnabled(false);
//          int returnVal = chooser.showOpenDialog(null);
//          if(returnVal == JFileChooser.APPROVE_OPTION) {
//            System.out.println("You chose "+
//    			   chooser.getSelectedFile().getAbsolutePath()+".");
//            jsch.addIdentity(chooser.getSelectedFile().getAbsolutePath()
////    			 , "passphrase"
//    			 );
//          }

//          String host=null;
//          if(arg.length>0){
//            host=arg[0];
//          }
//          else{
//            host=JOptionPane.showInputDialog("Enter username@hostname",
//                                             System.getProperty("user.name")+
//                                             "@localhost"); 
//          }
          String host = "hl677@40.121.57.154";
          String user=host.substring(0, host.indexOf('@'));
          host=host.substring(host.indexOf('@')+1);

          Session session=jsch.getSession(user, host, 22);
          session.setConfig("StrictHostKeyChecking", "no"); // disable check for RSA key


          // username and passphrase will be given via UserInfo interface.
//          UserInfo ui=new MyUserInfo();
//          session.setUserInfo(ui);
          //session.connect();
          //System.out.println(session.isConnected());
//          Channel channel=session.openChannel("shell");
    //
//          channel.setInputStream(System.in);
//          channel.setOutputStream(System.out);
    //S
          return session;
//          channel.connect();
        }
        catch(Exception e){
          System.out.println(e);
          return null;
          
        }
      }
    

    private static void closeConnection(ChannelExec channel, Session session) {
        try {
            channel.disconnect();
        } catch (Exception ignored) {
        }
        session.disconnect();
    }
}
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
import java.util.ArrayList;
import java.util.List;

class RunCommandViaSsh {

    private static final String SSH_HOST = "40.121.57.154";
    static String SSH_HOST2 = "40.121.233.18";
    //static String SSH_HOST3 = "";

    
    private static final String SSH_LOGIN = "hl677";
    private static final String SSH_PASSWORD = "";
    static String privateKey = "/Users/haoli/.ssh/id_rsa";
    
    static String command1 = "free -m | awk \'NR==2{printf \"Memory Usage: %s/%sMB (%.2f%%)\", $3,$2,$3*100/$2 }\' ";
    public static String command2 = "df -h | awk \'$NF==\"/\"{printf \"Disk Usage: %d/%dGB (%s)\", $3,$2,$5}\'";
    
    static String command3 = "awk -v a=\"$(awk \'/cpu /{print $2+$4,$2+$4+$5}\' /proc/stat; sleep 1)\" \'/cpu /{split(a,b,\" \"); print 100*($2+$4-b[1])/($2+$4+$5-b[2])}\'  /proc/stat";

    static List<String> lst = new ArrayList<>();
   
    static List<String> servers = new ArrayList<>();
    
    //public String command3 = 
    
    public static void main() throws JSchException, IOException {
    	//runCommand("pwd");
    	 //String memUsage =  "free -m | awk 'NR==2{printf \"Memory Usage: %s/%sMB (%.2f%%)\n\", $3,$2,$3*100/$2 }' ";
    	 //System.out.println(command1);
    	 //System.out.println(command2);
        //StringBuilder sb = new StringBuilder();

       //System.out.println(runCommand("ls -la"));
    	servers.add(SSH_HOST);
    	servers.add(SSH_HOST2);
   	 lst.add(command1);
   	 lst.add(command2);
   	 lst.add(command3);
    	for (String server : servers) {
    		runCommand(lst, server);
    	}

//    	runCommand(lst);
//    	//runCommand(command2);
    	//closeConnection(null, null);
    	System.out.println("done");
    	//setupSshSession();
    }
    
//    free -m | awk 'NR==2{printf "Memory Usage: %s/%sMB (%.2f%%)", $3,$2,$3*100/$2 }'
//    
//    free -m | awk 'NR==2{printf "Memory Usage: %s/%sMB (%.2f%%)", $3,$2,$3*100/$2 }' 


    private static void runCommand(List<String> commandList, String server) throws JSchException, IOException {
        Session session = setupSshSession(server);
        session.connect();
        //System.out.println(session.isConnected());
        
//        Channel channel = session.openChannel("shell");
////      Channel channel=session.openChannel("shell");
////
//      channel.setInputStream(System.in);
//      channel.setOutputStream(System.out);
//      System.out.println(channel.getOutputStream());
//      channel.connect();
        boolean flag = true;
        for (String command : commandList) { 
            Channel channel=session.openChannel("exec");
            
        	//System.out.println(command);
        	((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            
            InputStream in=channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            //boolean flag = true;
            while(flag){
              while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<=0)break;
                System.out.print(new String(tmp, 0, i));
                System.out.println("/n");
                flag = false;
                //System.out.println("hi");
              }
        }
        
            //System.out.println("here");
          flag = true;
          channel.disconnect();
          //break;

    }
        session.disconnect();

    }
    private static Session setupSshSession(String server) throws JSchException {
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
          //String host = "hl677@40.121.57.154";
      		String host = "hl677@" + server;
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
    

    private static void closeConnection(Channel channel, Session session) {
        try {
            channel.disconnect();
        } catch (Exception ignored) {
        }
        session.disconnect();
    }
}
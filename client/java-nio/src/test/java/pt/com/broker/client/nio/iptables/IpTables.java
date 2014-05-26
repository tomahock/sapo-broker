package pt.com.broker.client.nio.iptables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luissantos on 14-05-2014.
 */
public class IpTables {


    public enum  TCP_DENY{
        REJECT, DROP
    }


    java.lang.Runtime rt;

    String iptablesPath = "sudo /sbin/iptables";

    Pattern find_chains_pattern = Pattern.compile("Chain ([\\w-]+) .*");

    public IpTables() {

        rt = java.lang.Runtime.getRuntime();
    }

    public boolean hasPermission(){


        try {
            return exec("-L").waitFor() != 3;
        } catch (Exception e) {
            return false;
        }


    }


    public boolean flushChain(String name) throws IOException, InterruptedException{
        String[] params = {"-F", name};

        return exec(params).waitFor() == 0;
    }

    public boolean deleteChain(String name)  throws IOException, InterruptedException{

        String[] params = {"-X", name};

        return flushChain(name) && exec(params).waitFor() == 0;
    }

    public boolean addChain(String name) throws IOException, InterruptedException {

        String[] params = {"-N", name};

        return exec(params).waitFor() == 0;
    }

    public boolean chainExists(String name) throws IOException, InterruptedException {

        String[] params = {"-L", name};

        return exec(params).waitFor() == 0;

    }


    public boolean removeChainfromChain(String parent, String child)throws IOException, InterruptedException {

        String[] params = {"-D", parent, "-j", child};

        return exec(params).waitFor() == 0;
    }

    public boolean addChaintoChain(String parent, String child) throws IOException, InterruptedException {

        String[] params = {"-A", parent, "-j", child};

        return exec(params).waitFor() == 0;
    }



    public boolean removePortBlock(String chain, int port, TCP_DENY deny) throws IOException, InterruptedException {

        String[] params = {"-D", chain , "-p" , "tcp" , "--dport" , Integer.toString(port) , "-j", deny.name() };

        return exec(params).waitFor() == 0;
    }

    public boolean blockPort(String chain, int port, TCP_DENY deny) throws IOException, InterruptedException {

        String[] params = {"-A", chain , "-p" , "tcp" , "--dport" , Integer.toString(port) , "-j", deny.name() };

        return exec(params).waitFor() == 0;
    }

    public boolean blockPort(String chain, int port) throws IOException, InterruptedException {

        return blockPort(chain, port, TCP_DENY.DROP);

    }

    public boolean removePortBlock(String chain, int port) throws IOException, InterruptedException {

        return removePortBlock(chain, port, TCP_DENY.DROP);

    }


    public Collection<String> getChains() throws IOException, InterruptedException {


        Process p = exec("-L");


        BufferedReader reader = new BufferedReader (new InputStreamReader(p.getInputStream()));


        List<String> chains = new ArrayList<String>();


        String line;
        while ((line = reader.readLine()) != null) {
            Matcher m = find_chains_pattern.matcher(line);

            if(m.find()){
                chains.add(m.group(1));
            }
        }


        return chains;

    }


    protected Process exec(String parameter) throws IOException, InterruptedException {

        List<String> parameters = new ArrayList<String>(1);

        parameters.add(parameter);


        return exec(parameters);
    }


    protected Process exec(String [] _parameters) throws IOException, InterruptedException {

        List<String> parameters = new ArrayList<String>(_parameters.length);

        for(String param : _parameters){
            parameters.add(param);
        }


        return exec(parameters);
    }



    protected Process exec(Collection<String> parameters) throws IOException, InterruptedException {

        List<String> command = new ArrayList<String>();

        command.add(iptablesPath);

        command.addAll(parameters);

        ProcessBuilder pb = new ProcessBuilder(command);

        Process p = pb.start();


        return  p;
    }
}

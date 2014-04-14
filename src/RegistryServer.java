
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;


public class RegistryServer extends UnicastRemoteObject implements RegistryInterface{

	protected RegistryServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			RegistryInterface rInterface = new RegistryServer();
			int portnum=Integer.parseInt(args[1]);
			InetAddress iAddress=InetAddress.getByName(args[0]);
			SocketFactory sFactory=new SocketFactory(iAddress);
			Registry registry = LocateRegistry.createRegistry(portnum,null,sFactory);
			registry.bind("registryServer", rInterface);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static LinkedList<String> list=new LinkedList<String>();
	
	@Override
	public boolean RegisterServer(String name) {
		
		int len=list.size();
		
		list.add(name);
		System.out.println(list);
		if(len==0){
			return true;
		}
		return false;
	}


	@Override
	public String[] GetFileServers() {
		// TODO Auto-generated method stub
		String fservers[]=new String[list.size()];
		int i=0;
		for(String s:list){
			fservers[i++]=s;
		}
		return fservers;
	}

}

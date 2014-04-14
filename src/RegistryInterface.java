import java.rmi.*;

public interface RegistryInterface extends Remote {

boolean RegisterServer(String name) throws RemoteException;

String[] GetFileServers() throws RemoteException;

}
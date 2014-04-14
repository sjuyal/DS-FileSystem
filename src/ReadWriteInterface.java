import java.io.IOException;
import java.rmi.*;

public interface ReadWriteInterface extends Remote{

    // offset is a multiple of 64K
public int FileWrite64K(String filename, long offset, byte[] data) throws IOException, RemoteException;

// returns the number of chunks in the file
public long NumFileChunks(String filename) throws IOException, RemoteException;

public byte[] FileRead64K(String filename, long offset) throws IOException, RemoteException;

}
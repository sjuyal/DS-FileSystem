
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;

public class FileServer extends UnicastRemoteObject implements
		ReadWriteInterface {

	static boolean master = false;

	protected FileServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public int FileWrite64K(String filename, long offset, byte[] data)
			throws IOException, RemoteException {
		System.out.println("Writing..");
		if (master) {
			try {
				if (data.length < 65536)
					return -1;
				System.out.println(filename);
				File towrite = new File(filename);
				if (!towrite.isDirectory()) {
					towrite.mkdirs();
				}
				String dirpath = filename + "/";
				File chunkwrite = new File(dirpath + "chunk" + offset);
				FileOutputStream os = new FileOutputStream(chunkwrite);
				FileWriter fw = new FileWriter(chunkwrite);
				BufferedWriter bw = new BufferedWriter(fw);
				/*
				 * char[] convertedChar = new char[data.length]; for (int i = 0;
				 * i < data.length; i++) { convertedChar[i] = (char) data[i]; }
				 * // bw.write(data.toString()); bw.write(convertedChar);
				 */
				os.write(data);
				os.close();
				bw.close();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return data.length;
		}else
			return -2;
	}

	@Override
	public long NumFileChunks(String filename) throws IOException,
			RemoteException {
		File file = new File(filename);
		long length = 0;
		if (file.exists())
			length = file.listFiles().length;
		return length;
	}

	@Override
	public byte[] FileRead64K(String filename, long offset) throws IOException,
			RemoteException {
		System.out.println("Transferring...Chunk:"+offset);
		if (!master) {
			File towrite = new File(filename + "/chunk" + offset);
			FileInputStream is = new FileInputStream(towrite);
			byte[] chunk = new byte[65536];
			int len = is.read(chunk);
			byte chunkarray[] = new byte[len];
			for (int i = 0; i < len; i++)
				chunkarray[i] = chunk[i];
			is.close();
			// System.out.println(chunkarray.length);
			return chunkarray;
		} else
			return null;
	}

	public static void main(String[] args) {
		try {
			RegistryInterface regface;

			String ipaddress=args[0];
			int portnum = Integer.parseInt(args[1]);
			Registry registry = LocateRegistry.getRegistry(ipaddress, portnum);
			regface = (RegistryInterface) registry.lookup("registryServer");
			
			String serverName = "Server_"
					+ Calendar.getInstance().getTimeInMillis() / 1000;
			System.out.println(serverName + " at your service");
			
			boolean ret=regface.RegisterServer(serverName);
			Registry reg;
			if(ret){
				reg=LocateRegistry.getRegistry(ipaddress,portnum); 
				System.out.println("Im the Master!");
				master=true;
			}
			else{
				reg=LocateRegistry.getRegistry(ipaddress,portnum);
				System.out.println("Im a slave!");
			}
			ReadWriteInterface rInterface = new FileServer();
			reg.bind(serverName, rInterface);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

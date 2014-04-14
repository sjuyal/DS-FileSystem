import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReadWriteClient extends Thread {

	private static String filepath = "/home/shashank/workspace/index";
	private static int Size = 65536;
	private static String filename = "index";
	private static int i = 0;
	private static RandomAccessFile rfile;
	private static Boolean checkChunks[];
	private static Boolean checkServers[];

	public static int randInt(int min, int max) {
		// Usually this can be a field rather than a method variable
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReadWriteInterface rwface;
		RegistryInterface regInterface;
		try {
			String serverAddress = args[1];
			String serverPort = args[2];
			filepath = args[0];
			filename = new File(filepath).getName();
			int port = Integer.parseInt(serverPort);
			Registry registry = LocateRegistry.getRegistry(serverAddress, port);
			regInterface = (RegistryInterface) registry
					.lookup("registryServer");

			String fServers[] = regInterface.GetFileServers();
			registry = LocateRegistry.getRegistry(serverAddress, port);
			rwface = (ReadWriteInterface) registry.lookup(fServers[0]);

			File towrite = new File(filepath);
			FileInputStream is = new FileInputStream(towrite);
			byte[] chunk = new byte[Size];
			int chunkLen = 0;
			int offset = 0;
			// System.out.println(filename + ":" + filepath);
			int flag = 0;
			while ((chunkLen = is.read(chunk)) != -1) {
				byte chunkarray[] = new byte[chunkLen];

				for (int i = 0; i < chunkLen; i++)
					chunkarray[i] = chunk[i];
				int ret = rwface.FileWrite64K(filename, offset, chunkarray);
				if (ret == -2) {
					System.out.println("This is not the master fileserver!");
					flag = 1;
					break;
				}
				offset++;
			}
			is.close();
			if (flag == 1)
				return;
			/*
			 * String fserv=fServers[randInt(1, count)];
			 * System.out.println("Called:"+fserv);
			 * rwface=(ReadWriteInterface)registry.lookup(fserv);
			 */

			long countchunks = rwface.NumFileChunks(filename);

			File outdir = new File("./output");
			if (!outdir.exists()) {
				outdir.mkdirs();
			}

			String saddr = serverAddress;
			int prt = port;
			String[] fservers = fServers;
			// System.out.println(fservers.length+"---");
			checkServers = new Boolean[fservers.length];
			if (countchunks >= 1) {
				rfile = new RandomAccessFile("./output/" + filename, "rw");

				readChunks(saddr, prt, fservers, countchunks);
				System.out.println("Done Initially!");

				Boolean checkDone = true;
				while (checkDone) {
					List<Integer> remChunkIds = new ArrayList<Integer>();

					for (int i = 0; i < countchunks; i++) {
						System.out.println(i + ":" + checkChunks[i]);
						if (!checkChunks[i]) {
							remChunkIds.add(i);
						}
					}
					if (remChunkIds.size() >= 1) {
						String[] fnewservers;
						List<String> updatedFileServers = new ArrayList<String>();
						checkServers[0] = true;
						for (int i = 0; i < fservers.length; i++) {
							if (checkServers[i])
								updatedFileServers.add(fservers[i]);
						}
						fnewservers = updatedFileServers
								.toArray(new String[updatedFileServers.size()]);

						readRemChunks(saddr, prt, fnewservers, remChunkIds);
						fservers=fnewservers;
					}else
						checkDone = false;
				}
				rfile.close();
				System.out.println("Done");
			} else
				System.out.println("Nothing to read!!");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readChunks(final String saddr, final int prt,
			final String[] fservers, final long countchunks) {

		try {
			Thread t[] = new Thread[(int) countchunks];
			checkChunks = new Boolean[(int) countchunks];
			System.out.println(countchunks);
			for (i = 0; i < countchunks; i++) {
				int k = i;
				t[k] = new Thread() {
					public void run() {

						try {
							// System.out.println("---" + i);
							int x = (i + 1) % (fservers.length - 1);
							if (x == 0)
								x = fservers.length - 1;
							Registry registry = LocateRegistry.getRegistry(
									saddr, prt);
							ReadWriteInterface rwface;
							try {
								rwface = (ReadWriteInterface) registry
										.lookup(fservers[x]);
								byte[] serverchunk = rwface.FileRead64K(
										filename, i);
								if (serverchunk == null) {
									System.out
											.println("This is master fileserver. It cant handle this request!");
								}
								// System.out.println(i);
								rfile.seek(i * 65536);
								rfile.write(serverchunk);
								// System.out.println("writing :" + (i *
								// 65536));
								checkChunks[i] = true;
								checkServers[x] = true;
							} catch (RemoteException e) {
								System.out.println("Remote Exception");
								checkChunks[i] = false;
								checkServers[x] = false;
							} catch (NotBoundException e) {
								System.out.println("NotBoundException");
								checkChunks[i] = false;
								checkServers[x] = false;
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				t[k].start();
				t[k].join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static void readRemChunks(final String saddr, final int prt,
			final String[] fservers, final List<Integer> remChunkIds) {

		try {
			int countchunks = remChunkIds.size();
			Thread t[] = new Thread[(int) countchunks];

			// System.out.println(countchunks);
			int k = -1;
			for (final int i : remChunkIds) {
				k++;
				t[k] = new Thread() {
					public void run() {

						try {
							// System.out.println("---" + i);
							int x = (i + 1) % (fservers.length - 1);
							if (x == 0)
								x = fservers.length - 1;
							try {
								Registry registry = LocateRegistry.getRegistry(
										saddr, prt);
								ReadWriteInterface rwface;

								rwface = (ReadWriteInterface) registry
										.lookup(fservers[x]);
								byte[] serverchunk = rwface.FileRead64K(
										filename, i);
								if (serverchunk == null) {
									System.out
											.println("This is master fileserver. It cant handle this request!");
								}
								// System.out.println(i);
								rfile.seek(i * 65536);
								rfile.write(serverchunk);
								System.out.println("writing chunk:" + i);
								checkChunks[i] = true;
								checkServers[x] = true;
							} catch (RemoteException e) {
								System.out.println("Remote Exception");
								checkChunks[i] = false;
								checkServers[x] = false;
							} catch (NotBoundException e) {
								System.out.println("NotBoundException");
								checkChunks[i] = false;
								checkServers[x] = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				t[k].start();
				t[k].join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}

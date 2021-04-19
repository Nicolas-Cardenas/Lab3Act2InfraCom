import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;





public class ClienteUDP extends Thread{

	private int identificador;
	private int clientes;
	private int PUERTO;
	private Hash hash;
	private DatagramSocket clientesocket;
	private Timer temporizador; 
	private double numeroPaquetes;



	public ClienteUDP(int Puerto, int ID) throws SocketException {

		temporizador = new Timer();

		clientesocket = new DatagramSocket(Puerto);

		identificador =ID;

		hash= new Hash();

		PUERTO = Puerto;
	}

	public void run()  {

		try
		{
			//HASH del archivo 

			Socket socket = new Socket("192.168.163.128", PUERTO);
			DataInputStream dis =new DataInputStream(socket.getInputStream());
			String hashh =dis.readUTF();
			socket.close();
			dis.close();


			//RECIBIR ARCHIVO

			long tiempoInicio = System.currentTimeMillis();
			while(!clientesocket.isClosed())
			{
				byte [] dataARecibir = new byte[1000];
				DatagramPacket dataIn = new DatagramPacket(dataARecibir, dataARecibir.length);
				recibirArchivoTimer(dataIn);
			}
			long finalTiempo = System.currentTimeMillis();
			long tiempoTotal = finalTiempo - tiempoInicio;
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
			LocalDateTime n = LocalDateTime.now(); 
			String conversionTiempo = dtf.format(n);
			String nombre="Logs/Cliente/"+conversionTiempo+"LOGCliente"+(identificador)+".txt"; 
		
			
			//GENERAR LOG
			File f = new File("ArchivosRecibidos/Cliente"+identificador+"-Prueba.txt");
			String hashF = calcularHash("ArchivosRecibidos/Cliente"+identificador+"-Prueba.txt");
			generarLog(nombre, f.length(),numeroPaquetes, tiempoTotal, identificador, hashF);

			 



		}
		catch (Exception e) {

			e.printStackTrace();

		}

	}


	public String calcularHash(String ruta) throws IOException
	{
		return hash.calcularHash(ruta);
	}

	private void generarLog(String nombre, long tamanioArchivo, double pPaquetes, long tiempoT, int idC, String hashF) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter es = new PrintWriter(nombre, "UTF-8");
		es.println("Tamaño del archivo: "+tamanioArchivo+"B");
		es.println("Id Cliente de transferencia: "+idC);
		es.println("Tiempo Transferencia Total: "+tiempoT+"milisegundos");
		es.println("Cantidad de Paquetes Transmitidos: "+pPaquetes);
		es.println("Hash que se recibió: "+ hashF);
		es.close();	
	}

	public void recibirArchivoTimer(DatagramPacket dataIn) throws IOException
	{
		
		try 
		{
			clientesocket.receive(dataIn);
			temporizador.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(currentThread().isAlive() && currentThread()!=null)
					{
						currentThread().interrupt();
						clientesocket.close();
						temporizador.cancel();
					}		
				}
			}, 5000);
		}
		catch (Exception e) 
		{
			System.out.println("Socket closed");
		}

		String packets = new String(dataIn.getData());
		numeroPaquetes = Double.parseDouble(packets);

		FileOutputStream fos = new FileOutputStream("ArchivosRecibidos/Cliente"+identificador+"-Prueba.txt");

		for(double i =0; i<numeroPaquetes; i++)
		{
			temporizador.cancel();
			temporizador = new Timer();
			temporizador.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(currentThread().isAlive() && currentThread()!=null)
					{
						currentThread().interrupt();
						clientesocket.close();
						temporizador.cancel();
					}		
				}
			}, 5000);

			byte[] bytes = new byte[1024];
			DatagramPacket dataRecibir = new DatagramPacket(bytes, bytes.length);

			
			try 
			{
				clientesocket.receive(dataRecibir);
			}
			catch (Exception e) 
			{
				System.out.println("Socket closed");
				break;
			}
			
			byte [] data2 = new byte[dataRecibir.getLength()];
			fos.write(data2);

		}

		clientesocket.close();
		fos.close();
		
	}

}


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



public class ServidorUDP extends Thread  {

	private int idC;
	private int numeroArchivo;
	private DatagramSocket servidor;
	private String RUTA;
	private Hash hash;
	private int numeroClientes;
	private int PUERTO;
	private InetAddress direccion;



	public ServidorUDP(int idF, int Puerto, String rutaArchivo, int numerArch, int numeroCli) 
	{

		try {

			direccion = InetAddress.getByName("192.168.0.4");
			PUERTO = Puerto;
			idC = idF;
			servidor = new DatagramSocket();
			RUTA = rutaArchivo;
			hash = new Hash();
			numeroArchivo = numerArch;
			numeroClientes=numeroCli;

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}


	public void run()  {

		try 
		{
			//HASH
			String hashh = calcularHash(RUTA);
			ServerSocket s = new ServerSocket(PUERTO);
			Socket socket = s.accept();
			DataOutputStream dos =new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(hashh);
			s.close();
			
			//ENVIO ARCHIVO
			
			
			File archivo1Envio = new File(RUTA);
			String nombreArchivo=RUTA.substring(15, RUTA.length());
			long tamanio=archivo1Envio.length();

			double numeroPaquetes = Math.ceil(((int)archivo1Envio.length())/1024);
			String aa = String.valueOf(numeroPaquetes);
			byte [] dataAEnviar = aa.getBytes();

			DatagramPacket outaa =new DatagramPacket(dataAEnviar, dataAEnviar.length, direccion,PUERTO);
			servidor.send(outaa);	


			
			long tiempoInicio = System.currentTimeMillis();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
			LocalDateTime n = LocalDateTime.now(); 
			String conversionTiempo = dtf.format(n);
			String nombreLog="Logs/Servidor/"+conversionTiempo+"Servidor"+idC+".txt";
			

			FileInputStream fis = new FileInputStream(archivo1Envio);
			BufferedInputStream bis =new BufferedInputStream(fis);

			for(double i=0; i<numeroPaquetes; i++)
			{
				byte[] bytes = new byte[1024];

				bis.read(bytes,0, bytes.length);
				DatagramPacket out = new DatagramPacket(bytes, bytes.length, direccion, PUERTO);
				servidor.send(out);		
			}
			bis.close();
			servidor.close();
			long finalTiempo = System.currentTimeMillis();
			long tiempoTotal = finalTiempo - tiempoInicio;
			
			
			//GENERAR LOG

			generarLog(nombreLog, nombreArchivo, tamanio, numeroPaquetes, tiempoTotal, idC, hashh);

			 


		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}



	private String calcularHash(String ruta) throws IOException
	{
		return hash.calcularHash(ruta);
	}

	private void generarLog(String nombre, String nombreArchivo, long tamanioArchivo, double pPaquetes, long tiempo, int idC, String hashCalculado) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter es = new PrintWriter(nombre, "UTF-8");
		es.println("Nombre del archivo: "+nombreArchivo);
		es.println("Tamaño del archivo: "+tamanioArchivo+"B");
		es.println("Id Cliente transferencia: "+idC);
		es.println("Tiempo de transferencia Total: "+tiempo+"milisegundos");
		es.println("Paquetes Transmitidos: "+pPaquetes);
		es.println("Hash que se envio: "+ hashCalculado);
		es.close();

	}


}

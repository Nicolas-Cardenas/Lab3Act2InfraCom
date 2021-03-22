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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



public class ServidorUDP extends Thread  {

	
	

	private int id;
	private int numeroArchivo;
	private DatagramSocket servidor;
	private DataOutputStream dos;
	private DataInputStream dis;
	private String RUTA;
	private Hash hash;
	private int numeroClientes;
	private int fragmentos;
	private int PUERTO;
	private InetAddress address;


	public ServidorUDP(int idF, int Puerto, String rutaArchivo, int numerArch, int numeroCli, int pfragmentos) {
		
		try {
			address = InetAddress.getByName("localhost");
			PUERTO = Puerto;
			fragmentos = pfragmentos;
			this.id = idF;
			this.servidor = new DatagramSocket(Puerto);
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
			System.out.println("Servidor iniciado con el puerto:" + " " + servidor.getLocalPort());
			
			


			int paquetes=0;
			File archivo1Envio = new File(RUTA);
			String hashF =calcularHash(RUTA);
			dos.write(numeroClientes);
			dos.write(fragmentos);
			dos.write(hashF.length());
			dos.write(hashF.getBytes(StandardCharsets.UTF_8));
			String nombreArchivo = RUTA.substring(15,RUTA.length());
			
			
			long tamanio = archivo1Envio.length();
			dos.writeLong(tamanio);
			int idC = dis.read();
			
			
			
		
			byte[] arreglo = archivo1Envio.getName().getBytes();
			
			DatagramPacket peticion = new DatagramPacket(arreglo, arreglo.length, address, PUERTO);
			servidor.send(peticion);
			
			FileInputStream fis = new FileInputStream(archivo1Envio);
			
	        byte[] bArray = new byte[(int) archivo1Envio.length()];
            fis.read(bArray);
            fis.close();
			
			
			long tiempoInicio = System.currentTimeMillis();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
			LocalDateTime n = LocalDateTime.now(); 
			String conversionTiempo = dtf.format(n);
			String nombreLog="Logs/Servidor/"+conversionTiempo+"Servidor:"+id+".txt"; 
			
			int frags = fragmentos;
			
			for(int i=0;i<bArray.length && frags <= arreglo.length; i++)
			{
				byte[] message = new byte[1024];
				DatagramPacket sendPacket = new DatagramPacket(message, fragmentos, address,PUERTO);
				servidor.send(sendPacket);
				frags += fragmentos;
				paquetes++;
			}
			
			
			if(numeroArchivo==1)
			{
				dos.write(1);
				dos.flush();
			}
			else if (numeroArchivo==2)
			{
				dos.write(2);
				dos.flush();
			}
		
			
			long finalTiempo = System.currentTimeMillis();
			long tiempoTotal = finalTiempo - tiempoInicio;
			int estadoT=dis.read();
			
			generarLog(nombreLog, nombreArchivo, tamanio, paquetes, tiempoTotal, idC, estadoT, hashF);
			
			
			dis.close();
			dos.close();
			

		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	

	private String calcularHash(String ruta) throws IOException
	{
		return hash.calcularHash(ruta);
	}
	
	private void generarLog(String nombre, String nombreArchivo, long tamanioArchivo, int pPaquetes, long tiempo, int idC, int estado, String hashCalculado) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter es = new PrintWriter(nombre, "UTF-8");
		es.println("Nombre del archivo: "+nombreArchivo);
		es.println("Tamaño del archivo: "+tamanioArchivo+"B");
		es.println("Id Cliente transferencia: "+idC);
		es.println("Tiempo de transferencia Total: "+tiempo+"milisegundos");
		es.println("Paquetes Transmitidos: "+pPaquetes);
		es.println("Estado de transferencia: "+estado);
		es.println("Hash que se envio: "+ hashCalculado);
		es.close();
		
	}


}

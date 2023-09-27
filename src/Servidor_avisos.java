import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Servidor_avisos {
    public static void main(String[] args) throws Exception{
        Date dataHoraAtual = new Date();
        SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        String dataHoraFormatada = formatoDataHora.format(dataHoraAtual);

        String mensagem = " ";
        byte[] envio;
        Scanner scanner = new Scanner(System.in);

        MulticastSocket socket = new MulticastSocket();
        InetAddress grupo = InetAddress.getByName("224.0.0.1");


        while(!mensagem.equals("Servidor Encerrado!")){
            System.out.print("[Servidor] Envie uma mensagem para o canal de Novidade:");
            mensagem = scanner.nextLine();

            if(mensagem.equals("encerrar")) {
                mensagem = "Servidor Encerrado!";
            }

            String mensagemFormatada = "[" + dataHoraFormatada + "] " + "Avisos" + " : " + mensagem;

            envio = mensagemFormatada.trim().getBytes();

            DatagramPacket pacote = new DatagramPacket(envio, envio.length,grupo, 4551);
            socket.send(pacote);
        }

        System.out.print("[Servidor] Tópico de Novidades Encerrado");
        socket.close();
    }

}



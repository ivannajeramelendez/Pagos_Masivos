package com.convertidor.AplicarPago;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App 
{
	public static int FECHA_TRANSACCION=2;
	public static int NO_REFERENCIA=4;
	public static int IMPORTE=5;
	public static int NUMERO_AUTORIZACION=6;
	public static int ID_CLIENTE=8;
	public static int NOMBRE_TITULAR=9;
    public static void main( String[] args )
    {
    	try {
        	File f = new File("aprobados.xlsx");
            InputStream inp = new FileInputStream(f);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0); 
            int iRow=1;
            Row row = sheet.getRow(iRow); //En qué fila empezar ya dependerá también de si tenemos, por ejemplo, el título de cada columna en la primera fila
            while(row!=null) 
            {
            	 Cell fechaTransaccionCell = row.getCell(FECHA_TRANSACCION);  
                 Cell noReferenciaCell = row.getCell(NO_REFERENCIA); 
                 Cell importeCell = row.getCell(IMPORTE); 
                 Cell noAutorizacionCell = row.getCell(NUMERO_AUTORIZACION); 
                 Cell idClienteCell = row.getCell(ID_CLIENTE); 
                 Cell nombreTitularCell = row.getCell(NOMBRE_TITULAR);   
                 Date fechaTransaccionVal = fechaTransaccionCell.getDateCellValue();
                 String noReferenciaVal =  noReferenciaCell.getStringCellValue();
                 double importeVal = importeCell.getNumericCellValue();
                 String noAutorizacionVal = noAutorizacionCell.getStringCellValue();
                 int idClienteVal = (int) idClienteCell.getNumericCellValue();
                 String nombreTitularVal = nombreTitularCell.getStringCellValue();
                 System.out.println( fechaTransaccionVal+"\t|\t"+noReferenciaVal+"\t|\t"+importeVal+"\t|\t"+
       								 noAutorizacionVal  +"\t|\t"+idClienteVal   +"\t|\t"+nombreTitularVal+"\t|\t");
                 
                 String body="{\r\n"
                 		+ "\"IdCliente\":"+idClienteVal+",\r\n"
                 		+ "\"Token\":\"77D5BDD4-1FEE-4A47-86A0-1E7D19EE1C74\"\r\n"
                 		+ "}";
                 
                 
                 JSONObject resp = connectApi("http://192.168.20.26/ServiciosClubAlpha/api/Pagos/GetPedidoByCliente",body);
     		   	if(resp!=null) {
     		   		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
     		   		SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss.SSS");
     		   		int noPedido=resp.getInt("NoPedido");
     				System.out.println(noPedido);
     				String bodyPago="{\r\n"
     						+ "\"NoPedido\":"+noPedido+",\r\n"
     						+ "\"Monto\":"+importeVal+",\r\n"
     						+ "\"Notarjeta\":\"1111\",\r\n"
     						+ "\"FolioInterbancario\":\""+noReferenciaVal+"\",\r\n"
     						+ "\"NoAutorizacion\":\""+noAutorizacionVal+"\",\r\n"
     						+ "\"FechaPago\":\""+formatter.format(fechaTransaccionVal)+"\",\r\n"
     						+ "\"HoraPago\":\""+formatter2.format(fechaTransaccionVal)+"\",\r\n"
     						+ "\"TitularCuenta\":\""+nombreTitularVal+"\",\r\n"
     						+ "\"FormaPago\":8,\r\n"
     						+ "\"ReciboName\":\"%REC%\"\r\n"
     						+ "}";
                    resp = connectApi("http://192.168.20.57:8090/alpha/aplicarPago",bodyPago);
                    System.out.println(resp);
     		   	}
                 
                iRow++;  
                row = sheet.getRow(iRow);
            }
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	public static JSONObject connectApi(String endpoint,String body) {
		HttpRequest request1 = HttpRequest.newBuilder().uri(
				 URI.create(endpoint))
				 .header("Content-Type", "application/json")
				 .POST(BodyPublishers.ofString(body)).build();
		CompletableFuture<String> client = HttpClient.newHttpClient().sendAsync(request1, BodyHandlers.ofString())
				.thenApply(HttpResponse::body);
		String json = "";
		JSONObject resp=null;
		try {
			json = String.valueOf(client.get());
			if(!json.equals("")) {
				resp=new JSONObject(json);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return resp;
	}
}
            
           

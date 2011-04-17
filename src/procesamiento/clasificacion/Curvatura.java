package procesamiento.clasificacion;

import java.util.ArrayList;
import java.util.List;

import objeto.Objeto;
import objeto.Pixel;
import objeto.RasgoObjeto;

public class Curvatura extends EvaluadorRasgo {
	/**
	 * Porcentaje de la longitud del contorno de objeto con el cu�l se define el tama�o del segmento
	 */
	private int porcTamanioSegmento = 2;
	
	/**
	 * Angulo de variaci�n m�s all� del cu�l se concidera que la direcci�n del contorno cambia
	 */
	private int anguloDesvio = 30;

	public Curvatura() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public int getPorcTamanioSegmento() {
		return porcTamanioSegmento;
	}

	public void setPorcTamanioSegmento(int porcTamanioSegmento) {
		this.porcTamanioSegmento = porcTamanioSegmento;
	}

	public int getAnguloDesvio() {
		return anguloDesvio;
	}

	public void setAnguloDesvio(int anguloDesvio) {
		this.anguloDesvio = anguloDesvio;
	}
	
	/**
	 * Cociente entre la longitud del contorno y la cantidad de cambios de direccion del contorno
	 */
	public RasgoObjeto calcularValor(Objeto objeto) {
		List<Pixel> contorno = objeto.getContorno();
		int tamanioSegmento = (int)((double) getPorcTamanioSegmento() * contorno.size() / 100);
		double cantCambiosDireccion = 0;
		
		if (tamanioSegmento == 0)
			tamanioSegmento = 1;
		
		int posIniVentana = 0;
		int posFinVentana = tamanioSegmento;
		Pixel iniVentana = contorno.get(posIniVentana);
		Pixel finVentana = contorno.get(posFinVentana);
		//Para la ecuacion de la recta
		
		boolean parar = false;
		int i = posFinVentana;
		int inicio = i;
		while (!parar && contorno.size() > tamanioSegmento){
			
			Pixel p = contorno.get(i % contorno.size());
			Pixel finVentana2 = contorno.get((i + tamanioSegmento) % contorno.size());

			double pendiente1 = 0;
			if (finVentana.getXDouble() - iniVentana.getXDouble() != 0)
				pendiente1 = (finVentana.getYDouble() - iniVentana.getYDouble()) / (finVentana.getXDouble() - iniVentana.getXDouble());
			else 
				pendiente1 = 1;
			
			double pendiente2 = 0;
			if (finVentana2.getXDouble() - p.getXDouble() != 0)
				pendiente2 = (finVentana2.getYDouble() - p.getYDouble()) / (finVentana2.getXDouble() - p.getXDouble());
			else
				pendiente2 = 1;
			
			double tgAngulo = Math.abs((pendiente2 - pendiente1) / (1 + pendiente2 * pendiente1));
			double angulo = Math.toDegrees(Math.atan(tgAngulo));
			if (Math.abs(angulo) > 5 && Math.abs(angulo) < anguloDesvio ){
				cantCambiosDireccion++;
			}

			posIniVentana = (posIniVentana + 1) % contorno.size();
			posFinVentana = (posFinVentana + 1) % contorno.size();
			
			iniVentana = contorno.get(posIniVentana);
			finVentana = contorno.get(posFinVentana);
		
			i = (i + 1) % contorno.size();
			
			if (i == inicio)
				parar = true;
		}
		Double valor = -1.0;
		if (cantCambiosDireccion != 0)
			valor = (double) cantCambiosDireccion / contorno.size();
		
		return new RasgoObjeto(this.getRasgoClase().getRasgo(),valor);
	}
	public static void main(String[] args) {
		Objeto o = new Objeto();
		List<Pixel> contorno = new ArrayList<Pixel>();
		contorno.add(new Pixel(10,10,null));
		contorno.add(new Pixel(15,10,null));
		contorno.add(new Pixel(15,11,null));
		contorno.add(new Pixel(15,15,null));
		contorno.add(new Pixel(14,15,null));
		contorno.add(new Pixel(10,15,null));
		contorno.add(new Pixel(10,14,null));
		contorno.add(new Pixel(10,10,null));
		o.setContorno(contorno);
		
		Curvatura c = new Curvatura();
		double valor = c.calcularValor(o).getValor();
		System.out.println(valor);
	}
}

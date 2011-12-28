package procesamiento.clasificacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objeto.Clase;
import objeto.ClaseObjeto;
import objeto.Histograma;
import objeto.Objeto;
import objeto.RasgoClase;
import objeto.RasgoObjeto;
import dataAcces.ObjectDao;

public class Clasificador {
	public static String CLASE_INDETERMINADO = "INDETERMINADO";

	private Map<EvaluadorClase, List<Objeto>> clasificacion = new HashMap<EvaluadorClase, List<Objeto>>();
	
	private List<Objeto> clasificacionInicial = new  ArrayList<Objeto>();
	
	//private ObjetoReferencia objetoReferencia= null; 
	
	private Configuracion configuracion;
	
	private List<Clase> clases = null;

	private Integer cantidadObjetos = null;
	
	private List<Objeto> clasificadosIncorrectamente = new ArrayList<Objeto>();
	
	public Clasificador() {
		super();
		configuracion = ObjectDao.getInstance().findConfiguracion("MAICES");
	}

	public Map<EvaluadorClase, List<Objeto>> getClasificacion() {
		return clasificacion;
	}

	public void setClasificacion(Map<EvaluadorClase, List<Objeto>> clasificacion) {
		this.clasificacion = clasificacion;
	}

	/**
	 * Obtiene la clasificacion inicial
	 * @return
	 */
	public List<Objeto> getClasificacionInicial() {
		return clasificacionInicial;
	}
	
	/**
	 * Crea una copia de la clasificacion para guardarla como clasificacion inicial
	 * @param clasificacionInicial
	 */
	public void setClasificacionInicial(List<Objeto> clasificacion) {
		this.clasificacionInicial = new ArrayList<Objeto>();
		this.clasificadosIncorrectamente = new ArrayList<Objeto>();
		for(Objeto objeto: clasificacion){
			clasificacionInicial.add(objeto.clonar());
		}
	}

	public List<Objeto> getClasificadosIncorrectamente() {
		return clasificadosIncorrectamente;
	}

	public void setClasificadosIncorrectamente(
			List<Objeto> clasificadosIncorrectamente) {
		this.clasificadosIncorrectamente = clasificadosIncorrectamente;
	}

	public Configuracion getConfiguracion() {
		return configuracion;
	}


	public void setConfiguracion(Configuracion configuracion) {
		this.configuracion = configuracion;
	}

	/**
	 * Guarda la clasificacion en la base de datos
	 * @param objetos
	 */
	public void guardarClasificacion(){
		ObjectDao dao = ObjectDao.getInstance();
		Set<EvaluadorClase> clases = clasificacion.keySet();
		for(EvaluadorClase c: clases){
			List<Objeto> objetosClase = clasificacion.get(c);
			for(Objeto obj:objetosClase){
				dao.save(obj);
			}
			
			for(EvaluadorRasgo er: c.getRasgos()){
				if (er.getRasgoClase().getRangoVariable() == true)
					actualizarRasgoClase(er.getRasgoClase(), objetosClase);;
			}
			Clase clase = c.getClase();
			actualizarClase(clase,objetosClase);
		}
	}
	
	/**
	 * Actualiza los histogramas promedio de la clase con los del objeto
	 * @param clase
	 * @param objeto
	 */
	private void actualizarHistogramaPromedio(Clase clase, Objeto objeto, int cantObjetos){
		for(Histograma h: objeto.getHistogramas()){
			Histograma histoClase = clase.getHistograma(h.getTipo());
			if (histoClase != null){
				for(int i = 0; i < histoClase.getValores().length;i++){
					double valActual = histoClase.getValores()[i];
					double valNuevo = (valActual * cantObjetos + h.getValores()[i]) / (cantObjetos + 1);
					histoClase.getValores()[i] = valNuevo;
				}
				histoClase.actualizarValoresString();
			}
			else{
				clase.getHistogramas().add(h);
			}
		}
	}
	
	/**
	 * 
	 * @param clase
	 * @param objetosClase
	 */
	private void actualizarClase(Clase clase, List<Objeto> objetosClase) {
		int cantObjetos = clase.getCantidadObjetos();
		for(Objeto obj:objetosClase){
			actualizarHistogramaPromedio(clase, obj, cantObjetos);
			cantObjetos++;
		}
		cantObjetos = clase.getCantidadObjetos() + objetosClase.size();
		clase.setCantidadObjetos(cantObjetos);
		ObjectDao.getInstance().save(clase);

	}

	/**
	 * Actualiza los valores valor medio, desvio estandar, maximo y minimo del rasgo de una clase
	 * @param rasgoClase RasgoClase
	 * @param objetos Objetos pertenecientes a la clase
	 */
	private void actualizarRasgoClase(RasgoClase rasgoClase, List<Objeto> objetos){
		Double sumValor = 0.0;
		Double sumValorCuadrado = 0.0;
		Double maximo = rasgoClase.getMaximo();
		Double minimo = rasgoClase.getMinimo();
		Integer cantValores = 0;
		
		if(rasgoClase.getSumValor() != null)
			sumValor = rasgoClase.getSumValor();
		if(rasgoClase.getSumValorCuadrado() != null)
			sumValorCuadrado = rasgoClase.getSumValorCuadrado();
		if(rasgoClase.getCantValores() != null)
			cantValores = rasgoClase.getCantValores();

		for(Objeto o: objetos){
			RasgoObjeto ro = o.getRasgo(rasgoClase.getRasgo());
			if(ro != null && ro.getValor() != null){
				sumValor += ro.getValor();
				Double valorCuadrado = Math.pow(ro.getValor(), 2); 
				sumValorCuadrado += valorCuadrado;
				if ((maximo != null &&  ro.getValor() > maximo) || maximo == null)
					maximo = ro.getValor();
				if ((minimo != null &&  ro.getValor() < minimo) || minimo == null)
					minimo = ro.getValor();
			}
			cantValores++;
		}
		
		rasgoClase.setSumValor(sumValor);
		rasgoClase.setSumValorCuadrado(sumValorCuadrado);
		rasgoClase.setCantValores(cantValores);
		rasgoClase.setMinimo(minimo);
		rasgoClase.setMaximo(maximo);
		
		ObjectDao.getInstance().save(rasgoClase);		
	}
	
	/**
	 * Inicializa el hash de las clases
	 * @throws Exception 
	 */
	public void inicializarClasificacion() throws Exception{
		//objetoReferencia = new ObjetoReferencia();
		ObjetoReferencia.inicializarObjetoReferencia();
		ObjectDao dao = ObjectDao.getInstance();
		List<Clase> clases = dao.qryClases(null,false,false);
		clasificacion = new HashMap<EvaluadorClase, List<Objeto>>();
		
		this.cantidadObjetos = dao.getCantidadObjetos();
		
		for(Clase c: clases){
			EvaluadorClase ec = createEvaluadorClase(c);
			getClasificacion().put(ec, new ArrayList<Objeto>());
		}		
	}
	/**
	 * Recupera el evaluador de una clase
	 * @param nombreClase
	 * @return
	 */
	public EvaluadorClase getEvaluadorClase(String nombreClase){
		Clase c = ObjectDao.getInstance().findClase(nombreClase,null,null);
		EvaluadorClase ec = createEvaluadorClase(c);
		return ec;
		
	}
	
	/**
	 * Recupera el evaluador de una clase
	 * @param nombreClase
	 * @return
	 */
	public EvaluadorClase getEvaluadorClaseIndeterminado(){
		Clase c = ObjectDao.getInstance().findClase(null,true,null);
		EvaluadorClase ec = createEvaluadorClase(c);
		return ec;
		
	}

	/**
	 * Recupera el evaluador de una clase
	 * @param nombreClase
	 * @return
	 */
	public EvaluadorClase getEvaluadorClaseObjetoReferencia(){
		Clase c = ObjectDao.getInstance().findClase(null,null,true);
		EvaluadorClase ec = createEvaluadorClase(c);
		return ec;
		
	}

	/**
	 * Recupera el evaluador de una clase
	 * @param nombreClase
	 * @return
	 */
	private EvaluadorClase createEvaluadorClase(Clase c){
		List<EvaluadorRasgo> rasgos = new ArrayList<EvaluadorRasgo>();
		for(RasgoClase r: c.getRasgos()){
			try {
				if (r != null){
					if (r.getRasgo().getNombreEvaluadorRasgo() != null){
						Class evaluadorClass = Class.forName(r.getRasgo().getNombreEvaluadorRasgo());
						EvaluadorRasgo er = (EvaluadorRasgo) evaluadorClass.newInstance();
						er.setRasgoClase(r);

						rasgos.add(er);
						System.out.println(r.getRasgo() + ", minimo: " + er.getMinimo() + ", maximo: " + er.getMaximo());
					}
					else{
						EvaluadorRasgo er = new EvaluadorRasgo();
						er.setRasgoClase(r);

						rasgos.add(er);
						System.out.println(r.getRasgo() + ", minimo: " + er.getMinimo() + ", maximo: " + er.getMaximo());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		EvaluadorClase ec = new EvaluadorClase(c,rasgos);
		return ec;
		
	}

	public List<Clase> getClases() {
		if (clases == null){
			clases = ObjectDao.getInstance().qryAll(Clase.class.getName());
		}
		return clases;
	}
	
	public int countObject(){
		int count = 0;
		Set<EvaluadorClase> clases = getClasificacion().keySet();
		for(EvaluadorClase c: clases){
			List<Objeto> objetosClase = getClasificacion().get(c);
			count = count + objetosClase.size();
		}	
		return count;
	}
	
	/**
	 * Retorna los objetos asignados a una clase dada
	 * @param clase
	 * @return
	 */
	public List<Objeto> getObjetosClase(Clase clase){
		for(EvaluadorClase ec: getClasificacion().keySet()){
			if (ec.getClase().equals(clase))
				return getClasificacion().get(ec);
		}
		return null;
	}

	public Integer getCantidadObjetos() {
		return cantidadObjetos;
	}

	public void setCantidadObjetos(Integer cantidadObjetos) {
		this.cantidadObjetos = cantidadObjetos;
	}
	
	public void aumentarCantidadObjetos(){
		this.cantidadObjetos++;
	}
	
	/**
	 * Metodo que compara la clase actual del objeto con la clase que se asigno inicialmente
	 * y actualiza el error de clasificacion en caso de ser necesario.
	 * @param objeto
	 */
	public void modificarClasificacion(Objeto objeto){
		int indexOriginal = getClasificacionInicial().indexOf(objeto);
		Objeto original = getClasificacionInicial().get(indexOriginal);
		ClaseObjeto claseOriginal = original.getClases().get(0);
		ClaseObjeto claseActual = objeto.getClases().get(0);
		if (claseActual.getClase().equals(claseOriginal.getClase())){
			if (getClasificadosIncorrectamente().contains(objeto)){
				getClasificadosIncorrectamente().remove(objeto);
			}
		}else{
			if (!getClasificadosIncorrectamente().contains(objeto)){
				getClasificadosIncorrectamente().add(objeto);
			}
		}
	}
}

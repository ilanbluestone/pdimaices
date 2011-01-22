package procesamiento.clasificacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objeto.Clase;
import objeto.Objeto;
import objeto.RasgoClase;
import dataAcces.ObjectDao;

public class Clasificador {
	public static String CLASE_INDETERMINADO = "INDETERMINADO";

	private Map<EvaluadorClase, List<Objeto>> clasificacion = new HashMap<EvaluadorClase, List<Objeto>>();
	
	private ObjetoReferencia objetoReferencia= null; 
	
	private Configuracion configuracion;
	
	private List<Clase> clases = null;

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
	
	public ObjetoReferencia getObjetoReferencia() {
		return objetoReferencia;
	}

	public void setObjetoReferencia(ObjetoReferencia objetoReferencia) {
		this.objetoReferencia = objetoReferencia;
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
		}
	}
	
	private void init(){
		
	}
	
	/**
	 * Inicializa el hash de las clases
	 * @throws Exception 
	 */
	public void inicializarClasificacion() throws Exception{
		objetoReferencia = new ObjetoReferencia();
		
		ObjectDao dao = ObjectDao.getInstance();
		List<Clase> clases = dao.qryAllClases(CLASE_INDETERMINADO);
		clasificacion = new HashMap<EvaluadorClase, List<Objeto>>();
		
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
		Clase c = ObjectDao.getInstance().findClase(nombreClase);
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
						er.setObjetoReferencia(getObjetoReferencia());
						er.setRasgoClase(r);

						rasgos.add(er);
						System.out.println(r.getRasgo() + ", valor: " + er.getValor() + ", devEst: " + er.getDesvioEstandar());
					}
					else{
						EvaluadorRasgo er = new EvaluadorRasgo();
						er.setObjetoReferencia(getObjetoReferencia());
						er.setRasgoClase(r);

						rasgos.add(er);
						System.out.println(r.getRasgo() + ", valor: " + er.getValor() + ", devEst: " + er.getDesvioEstandar());
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

}
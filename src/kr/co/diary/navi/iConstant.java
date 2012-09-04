package kr.co.diary.navi;

/**
 *	��� ���� �������̽�
 */
public interface iConstant {

	/**
	 * �� ���̵�
	 */
	public final static String APP_ID = "8100CFD2";

	/**
	 * �� �׽�Ʈ Ű
	 */
	public final static String APP_KEY = "T77EDC52D13BB61";
	
	/**
	 * ��θ� ã�� url
	 */
	public final static String SEARCH_URL = "http://openapi.kt.com/maps/etc/RouteSearch?params=";	
	
	/**
	 * �ּ� ��ȯ url
	 */
	public final static String GEOCODE_URL ="http://maps.google.com/maps/api/geocode/json?sensor=false&language=ko&";
	
	/**
	 *	�ּҺ�ȯ ��û ���� ó����
	 */
	public final static String STATUS_OK ="OK";	
	
	/**
	 *	����Ʈ ����
	 */
	public final static int DEFAULT_LAT = (int)(37.566538 * 1E6);	

	/**
	 *	����Ʈ �浵
	 */
	public final static int DEFAULT_LNG =(int)(126.977953 * 1E6);		

}

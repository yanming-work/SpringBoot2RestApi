package com.test.core.util.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.core.util.ConvertUtil;
import com.test.core.util.StringUtil;
 
public class WebUtil {
	private static Logger logger = LoggerFactory.getLogger(WebUtil.class);
	/**
	 * 获取IP
	 * @param request
	 * @return
	 * 修改获取ip方法，对于10.内网段重新获取远程ip地址
	 */
	public static String getIpAddr(HttpServletRequest request) {
	    String ip = request.getHeader("x-forwarded-for");
 
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	        ip = request.getHeader("Proxy-Client-IP");
	    }
 
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	        ip = request.getHeader("WL-Proxy-Client-IP");
	    }
 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	    	ip = request.getHeader("HTTP_X_FORWARDED_FOR");
	    }
 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)||ip.startsWith("10.")) {
	    	ip = request.getRemoteAddr();
	    }
	    return ip;
	}
 
	/**
	 * 向客户端发送信息
	 * @param response
	 * @param data
	 */
	public static void sendData(HttpServletResponse response, String data) {
		PrintWriter printWriter = null;
		try {
			response.setCharacterEncoding("UTF-8");
			printWriter = response.getWriter();
			printWriter.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.flush();
				printWriter.close();
			}
		}
	}
 
	/**
	 * HttpURLConnection 发送请求
	 * @author qiulongjie
	 * @param uri
	 * @param method
	 * @param params
	 * @param encode
	 * @return
	 */
	public static String sendData(String uri,String method,String params,String encode){
		try {
			URL url = new URL(uri);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(SOCKET_TIMEOUT);
			// 设置允许输入输出
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			byte[] mydata = params.getBytes();
			// 设置请求报文头，设定请求数据类型
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			// 设置请求数据长度
			urlConnection.setRequestProperty("Content-Length", String.valueOf(mydata.length));
			// 设置POST方式请求数据
			urlConnection.setRequestMethod(method);
			OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write(mydata);
			outputStream.flush();
			outputStream.close();
			int responseCode = urlConnection.getResponseCode();
			if (responseCode == 200) {
				return changeInputStream(urlConnection.getInputStream(), encode);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
 
	/**
	 * HttpURLConnection 发送请求
	 * @author qiulongjie
	 * @param uri
	 * @param method
	 * @param params
	 * @param encode
	 * @return
	 */
	public static String sendData(String uri,String method,Map<String,Object> params,String encode,boolean isUrlencode){
		StringBuffer buffer = new StringBuffer();
		if (params != null && !params.isEmpty()) {
			if(isUrlencode){
				try {
					for (Map.Entry<String, Object> entry : params.entrySet()) {
						buffer.append(entry.getKey()).append("=")
						.append(URLEncoder.encode(String.valueOf(entry.getValue()),encode))
						.append("&");// 请求的参数之间使用&分割。
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}else{
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					buffer.append(entry.getKey()).append("=")
					.append(String.valueOf(entry.getValue()))
					.append("&");// 请求的参数之间使用&分割。
				}
			}
			buffer.deleteCharAt(buffer.length() - 1);
		}
		logger.info(buffer.toString());
		logger.info(uri);
		return sendData(uri, method, buffer.toString(), encode);
	}
 
	/**
	 * 把输入流转为字符串
	 * @author qiulongjie
	 * @param inputStream
	 * @param encode
	 * @return
	 */
	public static String changeInputStream(InputStream inputStream, String encode) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		String result = "";
		if (inputStream != null) {
			try {
				while ((len = inputStream.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
				result = new String(outputStream.toByteArray(), encode);
 
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	
	
	 /***
     * 获取URI的路径,如路径为http://www.babasport.com/action/post.htm?method=add, 得到的值为"/action/post.htm"
     * @param request
     * @return
     */
    public static String getRequestURI(HttpServletRequest request){    
        return request.getRequestURI();
    }
    /**
     * 获取完整请求路径(含内容路径及请求参数)
     * @param request
     * @return
     */
    public static String getRequestURIWithParam(HttpServletRequest request){    
        return getRequestURI(request) + (request.getQueryString() == null ? "" : "?"+ request.getQueryString());
    }
    /**
     * 添加cookie
     * @param response
     * @param name cookie的名称
     * @param value cookie的值
     * @param maxAge cookie存放的时间(以秒为单位,假如存放三天,即3*24*60*60; 如果值为0,cookie将随浏览器关闭而清除)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {       
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        if (maxAge>0) cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
   
    /**
     * 获取cookie的值
     * @param request
     * @param name cookie的名称
     * @return
     */
    public static String getCookieByName(HttpServletRequest request, String name) {
     Map<String, Cookie> cookieMap = WebUtil.readCookieMap(request);
        if(cookieMap.containsKey(name)){
            Cookie cookie = (Cookie)cookieMap.get(name);
            return cookie.getValue();
        }else{
            return null;
        }
    }
   
    protected static Map<String, Cookie> readCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (int i = 0; i < cookies.length; i++) {
                cookieMap.put(cookies[i].getName(), cookies[i]);
            }
        }
        return cookieMap;
    }
    /**
     * 去除html代码
     * @param inputString
     * @return
     */
    public static String HtmltoText(String inputString) {
        String htmlStr = inputString; //含html标签的字符串
        String textStr ="";
        java.util.regex.Pattern p_script;
        java.util.regex.Matcher m_script;
        java.util.regex.Pattern p_style;
        java.util.regex.Matcher m_style;
        java.util.regex.Pattern p_html;
        java.util.regex.Matcher m_html;         
        java.util.regex.Pattern p_ba;
        java.util.regex.Matcher m_ba;
       
        try {
            String regEx_script = "<[//s]*?script[^>]*?>[//s//S]*?<[//s]*?///[//s]*?script[//s]*?>"; //定义script的正则表达式{或<script[^>]*?>[//s//S]*?<///script> }
            String regEx_style = "<[//s]*?style[^>]*?>[//s//S]*?<[//s]*?///[//s]*?style[//s]*?>"; //定义style的正则表达式{或<style[^>]*?>[//s//S]*?<///style> }
            String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
            String patternStr = "//s+";
           
            p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); //过滤script标签

            p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); //过滤style标签
        
            p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); //过滤html标签
           
            p_ba = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);
            m_ba = p_ba.matcher(htmlStr);
            htmlStr = m_ba.replaceAll(""); //过滤空格
        
         textStr = htmlStr;
        
        }catch(Exception e) {
                    System.err.println("Html2Text: " + e.getMessage());
        }         
        return textStr;//返回文本字符串
     }
 
	// *********************org.apache.http.impl.client.HttpClients ******* start -----
 
	/**
	 * 利用org.apache.http.impl.client.HttpClients发送post请求
	 * @author qiulongjie
	 * @param uri
	 * @param params
	 * @return
	 */
	public static String postData(String uri,Map<String,Object> params){
		StringBuffer buffer = new StringBuffer();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				buffer.append(entry.getKey()).append("=")
				.append(String.valueOf(entry.getValue()))
				.append("&");// 请求的参数之间使用&分割。
			}
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return postData(uri,buffer.toString());
	}
 
	/**
	 * 利用org.apache.http.impl.client.HttpClients发送post请求
	 * @author qiulongjie
	 * @param uri
	 * @param params
	 * @return
	 */
	public static String postData(String uri,String params){
		StringEntity myEntity = new StringEntity(params,ContentType.create("application/x-www-form-urlencoded", Consts.UTF_8));// 构造请求数据
        return postInfo(uri,myEntity);
	}
 
	/**
	 * 利用org.apache.http.impl.client.HttpClients发送post请求  发送json数据
	 * @author qiulongjie
	 * @param uri
	 * @param json
	 * @return
	 */
	public static String postJOSN(String uri,String json){
        StringEntity myEntity = new StringEntity(json,ContentType.APPLICATION_JSON);// 构造请求数据
        return postInfo(uri,myEntity);
	}
 
	/**
	 * 利用org.apache.http.impl.client.HttpClients发送post请求
	 * @author qiulongjie
	 * @param uri
	 * @param myEntity
	 * @return
	 */
	public static String postInfo(String uri,StringEntity myEntity){
		CloseableHttpClient client = HttpClients.custom().setRetryHandler(retryHandler).build();
        //链接配置
        RequestConfig config = createRequestConfig();
        HttpPost post = new HttpPost(uri);
        post.setConfig(config);
        post.setEntity(myEntity);
        CloseableHttpResponse response = null;
        try {
            response = client.execute(post);
            if (null != response && response.getStatusLine().getStatusCode() == 200) {
            	HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
            return null;
        } catch (ClientProtocolException e) {
        	logger.error(e.toString());
        	e.printStackTrace();
        } catch (IOException e) {
        	logger.error(e.toString());
        	e.printStackTrace();
        }finally{
			closeHttp(client, response);
        }
		return null;
	}
 
	/**
	 * 以GET方式发送数据
	 * @author qiulongjie
	 * @param uri
	 * @return
	 */
	public static String sendInfoForGET(String uri){
		CloseableHttpClient client = HttpClients.custom().setRetryHandler(retryHandler).build();
        //链接配置
        RequestConfig config = createRequestConfig();
        HttpGet get = new HttpGet(uri);
        get.setConfig(config);
        CloseableHttpResponse response = null;
        try {
			response = client.execute(get);
            if (null != response && response.getStatusLine().getStatusCode() == 200) {
            	HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
            return null;
        } catch (ClientProtocolException e) {
        	logger.error(e.toString());
        	e.printStackTrace();
        } catch (IOException e) {
        	logger.error(e.toString());
        	e.printStackTrace();
        }finally{
			closeHttp(client, response);
        }
		return null;
	}
 
	/**
	 * 发送https请求 get方式
	 * @param uri
	 * @return
	 */
	public static String sendHttpsForGET(String uri){
		CloseableHttpClient client = createSSLClientDefault();
		//链接配置
		RequestConfig config = createRequestConfig();
		HttpGet get = new HttpGet(uri);
		get.setConfig(config);
		CloseableHttpResponse response = null;
		try {
			response = client.execute(get);
			if (null != response && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			}
			return null;
		} catch (ClientProtocolException e) {
			logger.error(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}finally{
			closeHttp(client, response);
		}
		return null;
	}
 
	/**
	 * 创建ssl请求
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static CloseableHttpClient createSSLClientDefault(){
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				//信任所有
				public boolean isTrusted(X509Certificate[] chain,String authType) {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			return HttpClients.custom().setRetryHandler(retryHandler).setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return HttpClients.custom().setRetryHandler(retryHandler).build();
	}
 
	/** 关闭 */
	private static void closeHttp(CloseableHttpClient client, CloseableHttpResponse response) {
		try {
			if (response != null){
				response.close();
			}
		} catch (IOException e) {
			logger.error(e.toString());
		} finally {
			try {
				if (client != null){
					client.close();
				}
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}
 
	/**
	 * 获取连接配置
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static RequestConfig createRequestConfig(){
		if( REQUEST_CONFIG == null ){
			synchronized (WebUtil.class){
				if( REQUEST_CONFIG == null ){
					REQUEST_CONFIG = RequestConfig.custom().
													setSocketTimeout(SOCKET_TIMEOUT).
													setConnectTimeout(CONNECT_TIMEOUT).
													setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT).
													setStaleConnectionCheckEnabled(true).
													build();
				}
			}
		}
		return REQUEST_CONFIG;
	}
 
	/**
	 * 重连次数
	 */
	private static final int RETRY_CONNECTION_COUNT = 3;
	/**
	 * 响应超时时间
	 */
	private static final int SOCKET_TIMEOUT = 10000;
	/**
	 * 链接超时时间
	 */
	private static final int CONNECT_TIMEOUT = 10000;
	/**
	 *
	 */
	private static final int CONNECT_REQUEST_TIMEOUT = 10000;
	/**
	 * 连接配置
	 */
	private static RequestConfig REQUEST_CONFIG = null;
 
 
	/**
	 * 重连处理器
	 */
	private static HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
        
        /**
         * exception异常信息；
         * executionCount：重连次数；
         * context：上下文
         */
        @Override
        public boolean retryRequest(IOException exception, int executionCount,HttpContext context) {
 
            if (executionCount >= RETRY_CONNECTION_COUNT) {//如果连接次数超过RETRY_CONNECTION_COUNT次，就不进行重复连接
                return false;
            }
            System.out.println(exception.getClass());
            if (exception instanceof SocketTimeoutException) {//响应超时
            	logger.warn("响应超时--重连接次数："+executionCount+"--导致重连接的异常："+exception+"--导致重连接的http.request："+((HttpClientContext) context).getAttribute("http.request").toString());
                return true;
            }
            if (exception instanceof UnknownHostException) {//未找到主机
            	logger.warn("未找到主机--重连接次数："+executionCount+"--导致重连接的异常："+exception+"--导致重连接的http.request："+((HttpClientContext) context).getAttribute("http.request").toString());
                return true;
            }
            if (exception instanceof ConnectTimeoutException) {//连接超时
            	logger.warn("连接超时--重连接次数："+executionCount+"--导致重连接的异常："+exception+"--导致重连接的http.request："+((HttpClientContext) context).getAttribute("http.request").toString());
                return true;
            }
			if (exception instanceof InterruptedIOException) {//io操作中断
				logger.warn("io操作中断（响应超时）--重连接次数："+executionCount+"--导致重连接的异常："+exception+"--导致重连接的http.request："+((HttpClientContext) context).getAttribute("http.request").toString());
				return true;
			}
            if (exception instanceof SSLException) {
                //SSL handshake exception
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // Retry if the request is considered idempotent
                return true;
            }
            return false;
        }
    };
	
    
    
    


    /**
     * 对字符串进行编码
     *
     * @param str 需要处理的字符串
     * @param encoding 编码方式
     * @return 编码后的字符串
     */
    public static String escape(String str, String encoding) throws UnsupportedEncodingException {
        if (StringUtil.isEmpty(str)) {
            return "";
        }
        char[] chars =ConvertUtil.bytesToChars(ConvertUtil.encodeBytes(str.getBytes(encoding), '%'));
        return new String(chars);
    }

    /**
     * 对字符串进行解码
     *
     * @param str 需要处理的字符串
     * @param encoding 解码方式
     * @return 解码后的字符串
     */
    public static String unescape(String str,String encoding){
        if(StringUtil.isEmpty(str)){
            return "";
        }
        return UrlUtil.decodeQuery(str, encoding);
    }



    /**
     * HTML标签转义方法
     *
     *  	空格	 &nbsp;
            <	小于号	&lt;
            >	大于号	&gt;
            &	和号	 &amp;
            "	引号	&quot;
            '	撇号 	&apos;
            ￠	分	 &cent;
            £	镑	 &pound;
            ¥	日圆	&yen;
            €	欧元	&euro;
            §	小节	&sect;
            ©	版权	&copy;
            ®	注册商标	&reg;
            ™	商标	&trade;
            ×	乘号	&times;
            ÷	除号	&divide;
     */
    public static String unhtml(String content) {
        if (StringUtil.isEmpty(content)) {
            return "";
        }
        String html = content;
        html = html.replaceAll("'", "&apos;");
        html = html.replaceAll("\"", "&quot;");
        html = html.replaceAll("\t", "&nbsp;&nbsp;");// 替换跳格
        html = html.replaceAll("<", "&lt;");
        html = html.replaceAll(">", "&gt;");
        return html;
    }
    public static String html(String content) {
        if (StringUtil.isEmpty(content)) {
            return "";
        }
        String html = content;
        html = html.replaceAll("&apos;", "'");
        html = html.replaceAll("&quot;", "\"");
        html = html.replaceAll("&nbsp;", " ");// 替换跳格
        html = html.replaceAll("&lt;", "<");
        html = html.replaceAll("&gt;", ">");
        return html;
    }

    
}
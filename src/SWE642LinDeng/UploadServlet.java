package SWE642LinDeng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
 
		HttpSession session = request.getSession();

		if ("status".equals(request.getParameter("c"))) { 
			doStatus(session, response); 
		} else { 
			doFileUpload(session, request, response);
		}
	}
	private void doFileUpload(HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		try {
			 
			UploadListener listener = new UploadListener(request
					.getContentLength());
			listener.start(); 
			 
			session.setAttribute("FILE_UPLOAD_STATS", listener
					.getFileUploadStats());
			session.setAttribute("bytesRead","0");
			 
			FileItemFactory factory = new MonitoredDiskFileItemFactory(listener);
			 
			ServletFileUpload upload = new ServletFileUpload(factory);
			 		
			List items = upload.parseRequest(request);
 
			listener.done();
			
			boolean hasError = false;
 
			for (Iterator i = items.iterator(); i.hasNext();) {
				FileItem fileItem = (FileItem) i.next();
				if (!fileItem.isFormField()) { 
					processUploadedFile(fileItem,session); 
					fileItem.delete(); 
				}
			}

			if (!hasError) { 
				sendCompleteResponse(response, null); 
			} else {
				sendCompleteResponse(response,
						"Could not process uploaded file. Please see log for details.");
			}
		} catch (Exception e) {
			sendCompleteResponse(response, e.getMessage());
		}
	}
	public void processUploadedFile(FileItem item,HttpSession session) {
  
		String fileName = item.getName().substring(
				item.getName().lastIndexOf("\\") + 1);
 
		File file = new File("C:\\temp\\", fileName);
		InputStream in;
		try {
			in = item.getInputStream();
 
			FileOutputStream out = new FileOutputStream(file);
			byte[] buffer = new byte[4096]; // To hold file contents
			int bytes_read;
			long bytesRead = Long.parseLong((String) session.getAttribute("bytesRead"));  
			while ((bytes_read = in.read(buffer)) != -1) // Read until EOF
			{
				out.write(buffer, 0, bytes_read);
				bytesRead+=(long)bytes_read;
				session.setAttribute("bytesRead",String.valueOf(bytesRead));
			}
			UploadListener.FileUploadStats fileUploadStats = (UploadListener.FileUploadStats) session
			.getAttribute("FILE_UPLOAD_STATS");
			long sizeTotal = fileUploadStats.getTotalSize(); 
			session.setAttribute("bytesRead",String.valueOf(sizeTotal));
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					;
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					;
				}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void doStatus(HttpSession session, HttpServletResponse response)
			throws IOException {
 
		response.addHeader("Expires", "0");
		response.addHeader("Cache-Control",
				"no-store, no-cache, must-revalidate");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
 
		UploadListener.FileUploadStats fileUploadStats = (UploadListener.FileUploadStats) session
				.getAttribute("FILE_UPLOAD_STATS");
		if (fileUploadStats != null) {
			//long +fileUploadStats.getBytesRead();
			//System.out.println(bytesProcessed);
			long bytesProcessed = fileUploadStats.getBytesRead(); 
			long sizeTotal = fileUploadStats.getTotalSize(); 
 
			long percentComplete = (long) Math
					.floor(((double) bytesProcessed / (double) sizeTotal) * 100.0);
 
			long timeInSeconds = fileUploadStats.getElapsedTimeInSeconds();
 
			double uploadRate = bytesProcessed / (timeInSeconds + 0.00001);
 
			double estimatedRuntime = sizeTotal / (uploadRate + 0.00001);
 
			response.getWriter().println("<b>Upload status:</b><br/>");
			if (bytesProcessed != sizeTotal) {
				response.getWriter().println(
						"<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: "
								+ percentComplete + "%;\"></div></div>");
				response.getWriter().println(
						"Finished: " + bytesProcessed + " Total: " + sizeTotal
								+ " bytes (" + percentComplete + "%) "
								+ (long) Math.round(uploadRate / 1024)
								+ " Kbs <br/>");
				response.getWriter().println(
						"Time past: " + formatTime(timeInSeconds) + " Total time: "
								+ formatTime(estimatedRuntime) + " Time left: "
								+ formatTime(estimatedRuntime - timeInSeconds)
								+ "<br/>");

			}
			if (bytesProcessed == sizeTotal) {
				response.getWriter().println(
						"<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: 100%;\"></div></div>");
				response.getWriter().println(
						"Saved: " + bytesProcessed + " Total size: " + sizeTotal
						+ " bytes (" + percentComplete + "%) <br/>");
			}
		}		
	}

	private void sendCompleteResponse(HttpServletResponse response,
			String message) throws IOException {
		if (message == null) {
			response
					.getOutputStream()
					.print(
							"<html><head><script type='text/javascript'>function killUpdate() { window.parent.killUpdate(''); }</script></head><body onload='killUpdate()'></body></html>");
		} else {
			response
					.getOutputStream()
					.print(
							"<html><head><script type='text/javascript'>function killUpdate() { window.parent.killUpdate('"
									+ message
									+ "'); }</script></head><body onload='killUpdate()'></body></html>");
		}
	}

	private String formatTime(double timeInSeconds) {
		long seconds = (long) Math.floor(timeInSeconds);
		long minutes = (long) Math.floor(timeInSeconds / 60.0);
		long hours = (long) Math.floor(minutes / 60.0);

		if (hours != 0) {
			return hours + "hours " + (minutes % 60) + "minutes "
					+ (seconds % 60) + "seconds";
		} else if (minutes % 60 != 0) {
			return (minutes % 60) + "minutes " + (seconds % 60) + "seconds";
		} else {
			return (seconds % 60) + " seconds";
		}
	}
}

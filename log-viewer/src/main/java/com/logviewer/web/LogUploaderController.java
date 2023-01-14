package com.logviewer.web;

import com.logviewer.data2.Log;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.FormSubmitEvent;
import java.io.File;
import java.util.List;

public class LogUploaderController extends AbstractRestRequestHandler {

    public static final String CFG_LOG_UPLOAD_ENABLED = "log-viewer.log-upload.enabled";

    private static final Logger LOG = LoggerFactory.getLogger(Log.class);

    @Autowired
    private Environment environment;

    @Endpoint(method = FormSubmitEvent.MethodType.POST)
    public void upload(HttpServletRequest request) throws Exception {

        if (!isLogUploadAllowed()) {
            throw new RestException(403, "log file upload is not allowed");
        }

        String dir = request.getParameter("dir");

        if (ServletFileUpload.isMultipartContent(request)) {

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            ServletFileUpload upload = new ServletFileUpload(factory);

            List<FileItem> formItems = upload.parseRequest(request);
            if (formItems != null && formItems.size() > 0) {
                for (FileItem item : formItems) {
                    if (!item.isFormField()) {
                        String fileName = System.currentTimeMillis() + "_" + new File(item.getName()).getName();
                        String filePath = dir + File.separator + fileName;
                        File storeFile = new File(filePath);
                        item.write(storeFile);
                        LOG.info("file: {} uploaded successfully", filePath);
                    }
                }
            }
        } else {
            throw new RestException(500, "not a multipart content");
        }
    }

    private boolean isLogUploadAllowed() {
        return environment.getProperty(CFG_LOG_UPLOAD_ENABLED, Boolean.class, true);
    }
}

package com.safe.system;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.safe.system.common.ApiException;
import com.safe.system.file.FileValidation;
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.junit.jupiter.api.Test;

class PdfSecurityTest {
  @Test
  void launchActionIsRejectedFromParsedObjectsEvenWhenNotAKey() throws Exception {
    byte[] bytes;
    try (var document = new PDDocument();
        var output = new ByteArrayOutputStream()) {
      var page = new PDPage();
      document.addPage(page);
      var link = new PDAnnotationLink();
      var action = new COSDictionary();
      action.setName(COSName.S, "Launch");
      action.setString(COSName.F, "untrusted-program");
      link.getCOSObject().setItem(COSName.A, action);
      page.getAnnotations().add(link);
      document.save(output);
      bytes = output.toByteArray();
    }
    assertThrows(ApiException.class, () -> FileValidation.pdf(bytes));
  }
}

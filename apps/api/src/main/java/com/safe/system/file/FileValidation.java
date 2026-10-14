package com.safe.system.file;

import com.safe.system.common.ApiException;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.*;

public final class FileValidation {
  private FileValidation() {}

  public static void pdf(byte[] bytes) throws IOException {
    try (var document = Loader.loadPDF(bytes)) {
      if (document.isEncrypted()
          || document.getNumberOfPages() < 1
          || document.getNumberOfPages() > 1000)
        throw ApiException.badRequest("PDF criptografado, vazio ou com mais de 1000 páginas");
      Set<COSBase> seen = Collections.newSetFromMap(new IdentityHashMap<>());
      visit(document.getDocumentCatalog().getCOSObject(), seen, 0);
    }
  }

  private static void visit(COSBase value, Set<COSBase> seen, int depth) {
    if (value == null || !seen.add(value)) return;
    if (value instanceof COSName name
        && Set.of(
                "JavaScript",
                "Launch",
                "EmbeddedFile",
                "Rendition",
                "Movie",
                "Sound",
                "SubmitForm",
                "ImportData",
                "GoToR")
            .contains(name.getName()))
      throw ApiException.badRequest("PDF contém ação ou conteúdo incorporado não permitido");
    if (depth > 100) throw ApiException.badRequest("Estrutura PDF excessivamente complexa");
    if (value instanceof COSObject o) visit(o.getObject(), seen, depth + 1);
    else if (value instanceof COSDictionary d) {
      for (var entry : d.entrySet()) {
        if (Set.of(
                "JS",
                "JavaScript",
                "Launch",
                "EmbeddedFiles",
                "OpenAction",
                "AA",
                "RichMedia",
                "XFA")
            .contains(entry.getKey().getName()))
          throw ApiException.badRequest("PDF contém ações ou anexos não permitidos");
        visit(entry.getValue(), seen, depth + 1);
      }
    } else if (value instanceof COSArray a) {
      for (COSBase v : a) visit(v, seen, depth + 1);
    }
  }

  public static void image(byte[] bytes) throws IOException {
    try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw ApiException.badRequest("Imagem não reconhecida");
      var reader = readers.next();
      try {
        reader.setInput(stream, true, true);
        int w = reader.getWidth(0), h = reader.getHeight(0);
        if (w < 1 || h < 1 || w > 8000 || h > 8000 || (long) w * h > 32000000L)
          throw ApiException.badRequest("Imagem excede dimensões permitidas");
        if (reader.read(0) == null) throw ApiException.badRequest("Imagem inválida");
      } finally {
        reader.dispose();
      }
    }
  }
}

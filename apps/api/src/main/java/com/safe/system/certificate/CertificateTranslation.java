package com.safe.system.certificate;

import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class CertificateTranslation {
  @Column(name = "title")
  public String title;

  @Column(name = "description")
  public String description;
}

package com.safe.system.professionalprofile;

import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class ProfessionalProfileTranslation {
  @Column(name = "title")
  public String title;

  @Column(name = "summary")
  public String summary;
}

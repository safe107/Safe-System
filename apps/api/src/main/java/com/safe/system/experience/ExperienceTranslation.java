package com.safe.system.experience;

import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class ExperienceTranslation {
  @Column(name = "role")
  public String role;

  @Column(name = "description")
  public String description;

  @Column(name = "responsibilities")
  public String responsibilities;

  @Column(name = "results")
  public String results;
}

package com.safe.system.skill;

import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class SkillTranslation {
  @Column(name = "name")
  public String name;

  @Column(name = "description")
  public String description;
}

package com.eudemon.ratelimiter.utils;

import java.util.List;

public class MappingClass4Test {

  List<MappingClassUnit4Test> persons;

  public List<MappingClassUnit4Test> getPersons() {
    return persons;
  }

  public void setPersons(List<MappingClassUnit4Test> persons) {
    this.persons = persons;
  }

  public static class MappingClassUnit4Test {

    private String name;
    private int age;
    private boolean male;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public boolean isMale() {
      return male;
    }

    public void setMale(boolean male) {
      this.male = male;
    }

    @Override
    public String toString() {
      return "[name=" + name + ";age=" + age + ";male=" + male + "]";
    }

  }

}


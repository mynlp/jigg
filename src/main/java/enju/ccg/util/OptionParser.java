package enju.ccg.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 * @author Katsuhiko Hayashi
 * 
 */
public class OptionParser {
  private final Class options;
  private final Map<String, Field> n2f = new HashMap<String, Field>();
  private final Map<String, Option> n2o = new HashMap<String, Option>();
  private final Set<String> requiredoption = new HashSet<String>();

  public OptionParser(Class options) {
    this.options = options;
    Option option;
    String name;
    for (Field field : this.options.getDeclaredFields()) {
      option = field.getAnnotation(Option.class);
      if (option == null) {
        continue;
      }
      name = option.name();
      n2f.put(name, field);
      n2o.put(name, option);
      if (option.required()) {
        requiredoption.add(name);
      }
    }
  }

  // TODO
  private void usage() {
    System.err.format("required !!\n\n");
    for (Option option : n2o.values()) {
      if (option.required())
        System.err.format("%s\t%s\n", option.name(), option.usage());
    }
  }

  public Object parse(String[] args) {
    try {
      Option option;
      Field field;
      Class fieldtype;
      String arg;
      String argval;
      Object obj = options.newInstance();
      Set<String> required = new HashSet<String>();
      for (int i = 0; i < args.length;) {
        arg = args[i++];
        if (arg.equals("-h")) {
          usage();
        }
        option = n2o.get(arg);
        if (option == null) {
          // System.err.print(String.format("Option Error: %s\n",
          // arg));
          continue;
        } else {
          if (option.required()) {
            required.add(option.name());
          }
          field = n2f.get(arg);
          fieldtype = field.getType();
          if (fieldtype == boolean.class) {
            field.setBoolean(obj, true);
          } else {
            if (i < args.length)
              argval = args[i++];
            else
              argval = null;
            if (argval != null)
              argval.trim();
            if (fieldtype == int.class) {
              field.setInt(obj, Integer.parseInt(argval));
            } else if (fieldtype == float.class) {
              field.setFloat(obj, Float.parseFloat(argval));
            } else if (fieldtype == double.class) {
              field.setDouble(obj, Double.parseDouble(argval));
            } else if (fieldtype == String.class) {
              field.set(obj, argval);
            } else {
            }
          }
        }
      }
      Iterator<String> iter = requiredoption.iterator();
      while (iter.hasNext()) {
        String name = iter.next();
        if (!required.contains(name)) {
          usage();
          System.exit(1);
        }
      }

      return obj;
    } catch (IllegalAccessException e) {
      usage();
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      usage();
      throw new RuntimeException(e);
    }
  }
}

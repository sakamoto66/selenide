package com.codeborne.selenide.selector;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class ByShadow {

  /**
   * Find target elements inside shadow-root that attached to shadow-host.
   * <br/> Supports inner shadow-hosts.
   *
   * <br/> For example: shadow-host &gt; inner-shadow-host &gt; target-element
   * (each shadow-host must be specified explicitly).
   *
   * @param target           CSS expression of target element
   * @param shadowHost       CSS expression of the shadow-host with attached shadow-root
   * @param innerShadowHosts subsequent inner shadow-hosts
   * @return A By which locates elements by CSS inside shadow-root.
   */
  @CheckReturnValue
  @Nonnull
  public static By cssSelector(String target, String... innerShadowHosts) {
    return new ByShadowCss(target, innerShadowHosts);
  }

  @ParametersAreNonnullByDefault
  public static class ByShadowCss extends By implements Serializable {
    private static final long serialVersionUID = -1230258723099459239L;

    private final String[] innerShadowHosts;
    private final String target;

    ByShadowCss(String target, String... innerShadowHosts) {
      // noinspection ConstantConditions
      if (target == null) {
        throw new IllegalArgumentException("Cannot find elements when the selector is null");
      }
      this.innerShadowHosts = innerShadowHosts;
      this.target = target;
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public WebElement findElement(SearchContext context) {
      final SearchContext host = findShadowTree(context);
      return getElementInsideShadowTree(host, target);
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public List<WebElement> findElements(SearchContext context) {
      final SearchContext host = findShadowTree(context);
      return getElementsInsideShadowTree(host, target);
    }

    private WebElement getElementInsideShadowTree(SearchContext host, String target) {
      if (isShadowRootAttached(host)) {
        WebElement targetWebElement = (WebElement) getJavascriptExecutor(host)
            .executeScript("return arguments[0].shadowRoot.querySelector(arguments[1])", host, target);
        if (targetWebElement == null) {
          throw new NoSuchElementException("The element was not found: " + target);
        }
        return targetWebElement;
      } else {
        throw new NoSuchElementException("The element is not a shadow host or has 'closed' shadow-dom mode: " + host);
      }
    }

    private boolean isShadowRootAttached(SearchContext host) {
      return getJavascriptExecutor(host).executeScript("return arguments[0].shadowRoot", host) != null;
    }

    @SuppressWarnings("unchecked")
    private List<WebElement> getElementsInsideShadowTree(SearchContext host, String sh) {
      return (List<WebElement>) getJavascriptExecutor(host)
          .executeScript("return arguments[0].shadowRoot.querySelectorAll(arguments[1])", host, sh);
    }

    private JavascriptExecutor getJavascriptExecutor(SearchContext context) {
      JavascriptExecutor jsExecutor;
      if (context instanceof JavascriptExecutor) {
        jsExecutor = (JavascriptExecutor) context;
      } else {
        jsExecutor = (JavascriptExecutor) ((WrapsDriver) context).getWrappedDriver();
      }
      return jsExecutor;
    }

    private SearchContext findShadowTree(final SearchContext context) {
      SearchContext host = context;
      if (context instanceof WebElement) {
        for (final String innerHost : innerShadowHosts) {
          host = getElementInsideShadowTree(host, innerHost);
        }
      } else if (innerShadowHosts.length == 0) {
        throw new NoSuchElementException("The element was not found: " + target);
      } else {
        host = context.findElement(By.cssSelector(innerShadowHosts[0]));
        for (int i = 1; i < innerShadowHosts.length; i++) {
          host = getElementInsideShadowTree(host, innerShadowHosts[i]);
        }
      }
      return host;
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public String toString() {
      StringBuilder sb = new StringBuilder("By.cssSelector: ");
      if (innerShadowHosts.length > 0) {
        sb.append(Arrays.toString(innerShadowHosts)).append(" ");
      }
      sb.append(target);
      return sb.toString();
    }
  }
}

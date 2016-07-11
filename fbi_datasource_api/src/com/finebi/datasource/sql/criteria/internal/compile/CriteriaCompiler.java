
package com.finebi.datasource.sql.criteria.internal.compile;


import com.finebi.datasource.api.TypedQuery;
import java.io.Serializable;

/**
 * Compiles a JPA criteria query into an executable {@link TypedQuery}.  Its single contract is the {@link #compile}
 * method.
 * <p>
 * NOTE : This is a temporary implementation which simply translates the criteria query into a JPAQL query string.  A
 * better, long-term solution is being implemented as part of refactoring the JPAQL/HQL translator.
 *
 * @author Steve Ebersole
 */
public class CriteriaCompiler implements Serializable {




}

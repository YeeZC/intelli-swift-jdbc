
package com.finebi.datasource.api.criteria;


import com.finebi.datasource.api.metamodel.Bindable;
import com.finebi.datasource.api.metamodel.SingularAttribute;

/**
 * Represents a simple or compound attribute path from a 
 * bound type or collection, and is a "primitive" expression.
 *
 * @param <X>  the type referenced by the path
 *
 * @since Advanced FineBI Analysis 1.0
 */
public interface Path<X> extends Expression<X> {

    /** 
     * Return the bindable object that corresponds to the
     * path expression.
     * @return bindable object corresponding to the path
     */
    Bindable<X> getModel();
    
    /**
     *  Return the parent "node" in the path or null if no parent.
     *  @return parent
     */
    Path<?> getParentPath();
	
    /**
     *  Create a path corresponding to the referenced 
     *  single-valued attribute.
     *  @param attribute single-valued attribute
     *  @return path corresponding to the referenced attribute
     */
    <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute);



    /**
     *  Create an expression corresponding to the type of the path.
     *  @return expression corresponding to the type of the path
     */
    Expression<Class<? extends X>> type();


    //String-based:
	
    /**
     *  Create a path corresponding to the referenced attribute.
     * 
     *  <p> Note: Applications using the string-based API may need to 
     *  specify the type resulting from the <code>get</code> operation in order
     *  to avoid the use of <code>Path</code> variables.
     *
     *  <pre>
     *     For example:
     *
     *     CriteriaQuery&#060;Person&#062; q = cb.createQuery(Person.class);
     *     Root&#060;Person&#062; p = q.from(Person.class);
     *     q.select(p)
     *      .where(cb.isMember("joe",
     *                         p.&#060;Set&#060;String&#062;&#062;get("nicknames")));
     *
     *     rather than:
     * 
     *     CriteriaQuery&#060;Person&#062; q = cb.createQuery(Person.class);
     *     Root&#060;Person&#062; p = q.from(Person.class);
     *     Path&#060;Set&#060;String&#062;&#062; nicknames = p.get("nicknames");
     *     q.select(p)
     *      .where(cb.isMember("joe", nicknames));
     *  </pre>
     *
     *  @param attributeName  name of the attribute
     *  @return path corresponding to the referenced attribute
     *  @throws IllegalStateException if invoked on a path that
     *          corresponds to a basic type
     *  @throws IllegalArgumentException if attribute of the given
     *           name does not otherwise exist
     */
    <Y> Path<Y> get(String attributeName);
}

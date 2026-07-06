/**
 * Eclipse Fennec OData – Metadata bundle (E1 Foundation).
 *
 * <p>Hosts the OData metadata aspects ({@code odata.ecore}) and the
 * {@code ODataAspectProvider}, which docks onto the Model Metadata Service via its
 * {@link org.eclipse.fennec.model.metadata.api.AspectProvider} SPI. The provider
 * registers with the {@link org.eclipse.fennec.model.metadata.api.MetadataWhiteboard}
 * and builds the per-EPackage OData profile on {@code registerPackage()}.
 *
 * <p>This package is the seed of E1; the concrete aspect model and provider follow
 * once {@code odata.ecore} is in place.
 */
package org.eclipse.fennec.odata.metadata;

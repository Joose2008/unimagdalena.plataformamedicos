Entities (Relacion)

1) uso de enums para los estatus, pues son variables que son fijas. 
2) Relacion Doctor - Specialty, unidireccional (no hay necesidad de hacer el @OneToMany desde specialty, pues no hay querys que lo indiquen)
3) Relacion Doctor - Schedule (Unidereccional así como la anterior, no hay necesidad de @OneToMany desde doctors)
4) Relacion Patient - Appointment (Unidireccional, lo mismo). Esto para no tener que ejecutar las listas en cada query.
5) Lo mismo aplica para las demás relaciones OneToMany, no hay necesidad de ponerlas en ambos lados. 
6) uso de records para las querys (Permite trabajar mejor en la capa service con las queries




using System;
using Microsoft.EntityFrameworkCore;

using {{.dotnet.namespace}}.Models;

namespace {{.dotnet.namespace}}.Models
{
    public class FruitsContext : DbContext
    {
        public FruitsContext(DbContextOptions<FruitsContext> options) : base(options)
        {

        }

        public DbSet<Fruit> Fruits { get; set; }

    }
}

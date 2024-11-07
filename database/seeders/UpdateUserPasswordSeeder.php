<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\User;
use Illuminate\Support\Facades\Hash;

class UpdateUserPasswordSeeder extends Seeder
{
    public function run()
    {
        User::where('email', 'shannon@example.com')
            ->update([
                'password' => Hash::make('password123')
            ]);
    }
}